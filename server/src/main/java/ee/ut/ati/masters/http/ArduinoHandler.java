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
	private HashSet<String> accessTokens;

	public ArduinoHandler() {
		log = Logger.getLogger(this.getClass());
		predict = new Predict(ConnectionFactory.getDataSource());
		accessTokens = new HashSet<String>();
		accessTokens.add("ed8975ee-a48f-45b8-a5e2-fd0af61cacf2");
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
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

					int idleTime = 0;
					SensorData previousSensorData = XmppDAO.getPreviousSensorData(ConnectionFactory.getDataSource(), receivedSensorData.getLocation());
					if (previousSensorData != null) {
						double temp = Double.NaN;
						double hall = Double.NaN;
						double light = Double.NaN;

						for (Data data : receivedSensorData.getData()) {
							switch (data.getType()) {
							case Data.TYPE_TEMPERATURE:
								temp = data.getValue();
								break;
							case Data.TYPE_HALL:
								hall = data.getValue();
								break;
							case Data.TYPE_LIGHT:
								light = data.getValue();
								break;
							}
						}
						Map<Integer, Data> previousDataMap = previousSensorData.getDataMap();
						if (temp != Double.NaN && hall != Double.NaN && light != Double.NaN) {
							TestFuzzyLogicEngine engine = new TestFuzzyLogicEngine();

							Data prevTempData = previousDataMap.get(Data.TYPE_TEMPERATURE);
							Data prevHallData = previousDataMap.get(Data.TYPE_HALL);
							Data prevLightData = previousDataMap.get(Data.TYPE_LIGHT);

							log.debug("Fuzzy logic initialize temp data: " + prevTempData);
							log.debug("Fuzzy logic initialize light data: " + prevLightData);
							//log.debug("Fuzzy logic initialize hall data: " + prevHallData);

							double prevTemp = prevTempData == null ? 0 : prevTempData.getValue();
							double prevLight = prevLightData == null ? 0 : prevLightData.getValue();
							engine.initialize(prevTemp, prevLight);
							idleTime = engine.calculatePredictTime(temp);
						}

						if (idleTime <= 0 || !predict.predictData(receivedSensorData, idleTime)) {
							idleTime = 5;
						}
					}
					for (Data receivedData : receivedSensorData.getData()) {
						XmppDAO.insertSensorData(ConnectionFactory.getDataSource(), receivedSensorData.getLocation(), receivedData);
					}

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
}