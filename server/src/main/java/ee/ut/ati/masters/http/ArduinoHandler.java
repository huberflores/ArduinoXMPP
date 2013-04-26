package ee.ut.ati.masters.http;

import ee.ut.ati.masters.fuzzy.TestFuzzyLogicEngine;
import ee.ut.ati.masters.h2.ConnectionFactory;
import ee.ut.ati.masters.h2.XmppDAO;
import ee.ut.ati.masters.h2.data.Data;
import ee.ut.ati.masters.h2.data.SensorData;
import ee.ut.ati.masters.predict.Predict;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class ArduinoHandler extends AbstractHandler {

	private Logger log;
	private Predict predict;

	public ArduinoHandler() {
		log = Logger.getLogger(this.getClass());
		predict = new Predict(ConnectionFactory.getDataSource());
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		log.debug("handle called");
		long startTime = System.currentTimeMillis();
		if ("post".equalsIgnoreCase(request.getMethod())) {
			log.debug("New post request received");

			if ("application/json".equals(request.getContentType())) {
				try {
					String body = readRequestBody(request.getInputStream());
					SensorData receivedSensorData = XmppDAO.processMessage(body);
					if (receivedSensorData == null) {
						throw new IOException("Could not parse data");
					}
					log.info("Data successfully parsed");

					int idleTime = 10; // Default value

					Map<Integer, Predict.PredictionData> predictionMap = predict.createPredictionData(receivedSensorData);
					Predict.PredictionData tempPred = predictionMap.get(Data.TYPE_TEMPERATURE);
					Predict.PredictionData hallPred = predictionMap.get(Data.TYPE_HALL);
					Predict.PredictionData lightPred = predictionMap.get(Data.TYPE_LIGHT);

					if (areAllPredictionsValid(tempPred, lightPred, hallPred)) {
						double prevTemp = tempPred.getLastData().getValue();
						double prevHall = hallPred.getLastData().getValue();
						double prevLight = lightPred.getLastData().getValue();

						double measuredTemp = tempPred.getMeasuredData().getValue();
						double measuredHall = hallPred.getMeasuredData().getValue();
						double measuredLight = lightPred.getMeasuredData().getValue();

						log.debug("Fuzzy logic initialize data: temp = " + prevTemp + ", light = " + prevLight + ", hall = " + prevHall);
						log.debug("Measured data: temp = " + measuredTemp + ", light = " + measuredLight + ", hall = " + measuredHall);

						TestFuzzyLogicEngine engine = new TestFuzzyLogicEngine();
						engine.initialize(prevTemp, prevLight, prevHall);
						idleTime = engine.calculatePredictTime(
								new TestFuzzyLogicEngine.DataHolder(measuredTemp, tempPred.getPredictability()),
								new TestFuzzyLogicEngine.DataHolder(measuredLight, lightPred.getPredictability()),
								new TestFuzzyLogicEngine.DataHolder(measuredHall, hallPred.getPredictability()));
						log.debug("Fuzzy engine idle time = " + idleTime);
						if (idleTime > 10 && !predict.predictData(idleTime, tempPred, lightPred, hallPred)) {
							idleTime = 10;
						}
					}
					for (Data receivedData : receivedSensorData.getData()) {
						XmppDAO.insertSensorData(ConnectionFactory.getDataSource(), receivedSensorData.getLocation(), receivedData);
					}
					idleTime = Math.max(idleTime, 10); // Minimum value is 10seconds
					idleTime = 10;

					log.debug("Request handled in " + (System.currentTimeMillis() - startTime) + " ms");
					response.setContentType("application/json;charset=utf-8");
					response.setStatus(HttpServletResponse.SC_OK);
					String respString = String.format("{\"idle\": %d}", idleTime);
					log.debug("Response: " + respString);
					response.setContentLength(respString.getBytes("utf-8").length);
					response.getWriter().println(respString);
				} catch (IOException e) {
					log.error(e);
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} else {
				response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			}
		} else {
			log.debug("Unsupported request type received");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
		baseRequest.setHandled(true);
	}

	private String readRequestBody(InputStream in) throws IOException {
		if (in == null) {
			log.debug("InputStream is null");
			return "";
		}

		InputStreamReader reader = new InputStreamReader(in);

		StringBuilder body = new StringBuilder();
		char[] buffer = new char[4096];
		int read;
		while ((read = reader.read(buffer)) != -1) {
			body.append(buffer, 0, read);
		}
		log.debug("Received message body= " + body.toString());
		reader.close();
		return body.toString();
	}

	private static boolean areAllPredictionsValid(Predict.PredictionData... predictions) {
		for (Predict.PredictionData pred : predictions) {
			if (pred.getMeasuredData() == null || pred.getRegression() == null || pred.getLastData() == null
					|| pred.getType() == Predict.PredictionData.UNDEFINED
					|| pred.getPredictability() == Predict.PredictionData.UNDEFINED
					|| pred.getPredictability() < Predict.PREDICTABILITY_THRESHOLD
					|| pred.getLocation() == Predict.PredictionData.UNDEFINED) {
				return false;
			}
		}
		return true;
	}
}