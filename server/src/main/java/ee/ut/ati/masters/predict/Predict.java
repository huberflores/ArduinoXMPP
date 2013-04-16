package ee.ut.ati.masters.predict;

import ee.ut.ati.masters.h2.ConnectionFactory;
import ee.ut.ati.masters.h2.XmppDAO;
import ee.ut.ati.masters.h2.data.Data;
import ee.ut.ati.masters.h2.data.SensorData;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Predict {

	private Logger log = Logger.getLogger(this.getClass());
	private DataSource dataSource;
	private SimpleRegression regression;
	private List<Data> predictions;

	public Predict(DataSource source) {
		this.dataSource = source;
		regression = new SimpleRegression();
		predictions = new ArrayList<Data>();
	}

	/**
	 * Returns false if data could not be predicted (when data is not significant)
	 * @param receivedSensorData
	 * @param time
	 * @return
	 */
	public boolean predictData(SensorData receivedSensorData, int time) {
		if (time == 0) {
			log.debug("predictData called with time= " + time + " which is too small");
			return false;
		}
		predictions.clear();
		regression.clear();
		for (Data data : receivedSensorData.getData()) {
			log.debug("Predicting data, new Data: " + data);
			if (data.getType() != Data.TYPE_TEMPERATURE) {
				// Skip other data types...
				continue;
			}
			List<Data> comparisonData = XmppDAO.getDifferenceComparisonDataList(ConnectionFactory.getDataSource(), data.getType(), receivedSensorData.getLocation());
			if (!comparisonData.isEmpty() && comparisonData.size() > 1) {
				regression = new SimpleRegression();
				for (Data comparisonItem : comparisonData) {
					log.debug("Regression item: " + comparisonItem);
					regression.addData(comparisonItem.getMeasureTime().getTime(), comparisonItem.getValue());
				}
				regression.addData(data.getMeasureTime().getTime(), data.getValue());
				log.debug("Regression item: " + data);
				log.debug("Found " + regression.getN() + " samples");
				log.debug("Regression slope is : " + regression.getSlope());

				/*
				log.debug("Regression R is " + regression.getR());
				log.debug("Regression R2 " + regression.getRSquare());

				log.debug("Regression regressionSumSquared " + regression.getRegressionSumSquares());
				log.debug("Regression errorSumSquared " + regression.getSumSquaredErrors());


				double rmse = Math.sqrt(mse);
				double rmse2 = Math.sqrt(Math.sqrt(regression.getRegressionSumSquares() / regression.getN()));

				log.debug("Regression rmse " + rmse);
				log.debug("Regression rmse2 " + rmse2);
				*/

				if (!isRegressionValid()) {
					// We will not predict anything
					return false;
				}
				createPredictions(data, time);
			}
		}
		log.debug(predictions.size() + " predictions total");
		int location = receivedSensorData.getLocation();
		for (Data prediction : predictions) {
			XmppDAO.insertSensorData(dataSource, location, prediction);
		}
		return true;
	}

	private boolean isRegressionValid() {
		double rmse = Math.sqrt(regression.getMeanSquareError());
		log.debug("Regression root mean square error " + rmse);
		try {
			TDistribution dist = new TDistributionImpl(regression.getN() - 2);
			double alphaM = 2.0 * dist.cumulativeProbability(0.5 / rmse) - 1;
			log.debug("Regression alphaM = " + alphaM);
			if (alphaM <= 0.9) {
				log.warn("Regression alphaM is too low, predictions not accurate enough");
				return false;
			}
		} catch (MathException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void createPredictions(Data data, int time) {
		int step = 10; // 10 seconds
		int count = Math.max((time / 10), 0);
		long baseTime = data.getMeasureTime().getTime();
		for (int index = 1; index < count; index++) {
			long newTime = baseTime + step * index * 1000;
			double newValue = regression.predict(newTime);
			Data dataDao = new Data(data.getType(), newValue);
			dataDao.setMeasureTime(new Timestamp(newTime));
			dataDao.setMeasured(false);
			predictions.add(dataDao);
		}
	}
}
