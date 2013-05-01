package ee.ut.ati.masters.predict;

import ee.ut.ati.masters.h2.ConnectionFactory;
import ee.ut.ati.masters.h2.XmppDAO;
import ee.ut.ati.masters.h2.data.Data;
import ee.ut.ati.masters.h2.data.Sensor;
import ee.ut.ati.masters.h2.data.SensorData;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Predict {

	public static final double PREDICTABILITY_THRESHOLD = 0.9;

	private Logger log = Logger.getLogger(this.getClass());
	private DataSource dataSource;
	private Map<Integer, Sensor> sensorMap;

	public Predict(DataSource source, Map<Integer, Sensor> sensorMap) {
		this.dataSource = source;
		this.sensorMap = sensorMap;
	}

	public Map<Integer, PredictionData> createPredictionData(SensorData receivedSensorData) {
		HashMap<Integer, PredictionData> result = new HashMap<Integer, PredictionData>();
		for (Data data : receivedSensorData.getData()) {
			log.debug("Predicting data, new Data: " + data);

			PredictionData prediction = new PredictionData();
			List<Data> comparisonData = XmppDAO.getDifferenceComparisonDataList(ConnectionFactory.getDataSource(), data.getType(), receivedSensorData.getLocation());
			if (comparisonData.isEmpty() || comparisonData.size() < 2) {
				result.put(data.getType(), prediction);
				continue;
			}

			for (Data comparisonItem : comparisonData) {
				log.debug("Regression item: " + comparisonItem);
				prediction.regression.addData(comparisonItem.getMeasureTime().getTime(), comparisonItem.getValue());
			}
			prediction.regression.addData(data.getMeasureTime().getTime(), data.getValue());
			log.debug("Regression item: " + data);
			log.debug("Found " + prediction.regression.getN() + " samples");
			log.debug("Regression slope is : " + prediction.regression.getSlope());

			Sensor sensor = sensorMap.get(data.getType());
			if (sensor == null) {
				log.error("Sensor of type " + data.getType() + " not found in sensorMap");
				continue;
			}
			prediction.predictability = calculatePredictability(prediction.regression, sensor.getRegressionError());
			prediction.lastData = comparisonData.get(comparisonData.size() - 1);    // Last value
			prediction.location = receivedSensorData.getLocation();
			prediction.measuredData = data;
			prediction.type = data.getType();
			result.put(data.getType(), prediction);
		}
		return result;
	}

	/**
	 *
	 * @param predictions
	 * @param time
	 * @return
	 */
	public boolean predictData(int time, PredictionData... predictions) {
		if (time == 0) {
			log.debug("predictData called with time= " + time + " which is too small");
			return false;
		}
		List<Data> predictedData = new ArrayList<Data>();
		int location = -1;

		for (PredictionData pred : predictions) {
			if (location != -1 && location != pred.getLocation()) {
				log.warn("predictData called but PredictionData locations do not match");
				return false;
			}
			location = pred.getLocation();

			SimpleRegression reg = pred.getRegression();
			log.debug("Predicting data, new Data: " + pred.getMeasuredData());
			log.debug("Regression has " + reg.getN() + " samples");
			log.debug("Regression slope is : " + reg.getSlope());
			if (pred.predictability < PREDICTABILITY_THRESHOLD) {
				// We will not predict anything
				log.debug("Regression predictions are not accurate enough, skipping");
				return false;
			}
			predictedData.addAll(createPredictions(pred, time));
		}
		log.debug(predictedData.size() + " predictions total");
		for (Data prediction : predictedData) {
			XmppDAO.insertSensorData(dataSource, location, prediction);
		}
		return true;
	}

	private double calculatePredictability(SimpleRegression regression, double error) {
		try {
			log.debug("Regression error = " + error);
			double rmse = Math.sqrt(regression.getMeanSquareError());
			regression.getRegressionSumSquares();
			regression.getSumSquaredErrors();
			log.debug("Regression root mean square error " + rmse);
			TDistribution dist = new TDistributionImpl(regression.getN() - 2);
			double alphaM = 2.0 * dist.cumulativeProbability(error / rmse) - 1;
			log.debug("Regression alphaM = " + alphaM);
			return alphaM;
		} catch (MathException e) {
			e.printStackTrace();
			return 0;
		}
	}

	private List<Data> createPredictions(PredictionData data, int time) {
		List<Data> predictions = new ArrayList<Data>();
		int step = 10; // 10 seconds
		int count = Math.max((time / 10), 0);
		long baseTime = data.getMeasuredData().getMeasureTime().getTime();
		for (int index = 1; index < count; index++) {
			long newTime = baseTime + step * index * 1000;
			double newValue = data.getRegression().predict(newTime);
			Data dataDao = new Data(data.getMeasuredData().getType(), newValue);
			dataDao.setMeasureTime(new Timestamp(newTime));
			dataDao.setMeasured(false);
			predictions.add(dataDao);
		}
		return predictions;
	}

	public static class PredictionData {

		public static int UNDEFINED = -1;

		private SimpleRegression regression;
		private Data lastData;
		private Data measuredData;
		private double predictability; // in per cent
		private int location;
		private int type;

		public PredictionData() {
			regression = new SimpleRegression();
			predictability = UNDEFINED;
			location = UNDEFINED;
			type = UNDEFINED;
		}

		public PredictionData(SimpleRegression regression, Data lastData, Data measuredData, double predictability, int location) {
			this.regression = regression;
			this.lastData = lastData;
			this.measuredData = measuredData;
			this.predictability = predictability;
			this.location = location;
		}

		public SimpleRegression getRegression() {
			return regression;
		}

		public Data getLastData() {
			return lastData;
		}

		public Data getMeasuredData() {
			return measuredData;
		}

		public double getPredictability() {
			return predictability;
		}

		public int getLocation() {
			return location;
		}

		public int getType() {
			return type;
		}
	}
}
