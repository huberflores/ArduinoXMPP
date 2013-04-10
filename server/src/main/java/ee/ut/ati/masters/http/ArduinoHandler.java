package ee.ut.ati.masters.http;

import ee.ut.ati.masters.fuzzy.TestFuzzyLogicEngine;
import ee.ut.ati.masters.h2.XmppDAO;
import ee.ut.ati.masters.h2.data.Data;
import ee.ut.ati.masters.h2.data.SensorData;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

public class ArduinoHandler extends AbstractHandler {

	private Logger log = Logger.getLogger(this.getClass());
	private TestFuzzyLogicEngine engine = new TestFuzzyLogicEngine();

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		long startTime = System.currentTimeMillis();
		if ("post".equalsIgnoreCase(request.getMethod())) {
			log.debug("New post request received");

			if ("application/json".equals(request.getContentType())) {
				try {
					ServletInputStream in = request.getInputStream();
					if (in == null) {
						log.debug("InputStream is null");
					}

					InputStreamReader reader = new InputStreamReader(in);

					StringBuilder body = new StringBuilder();
					char[] buffer = new char[4096];
					int totalRead = 0;
					int read = 0;
					while ((read = reader.read(buffer)) != -1) {
						body.append(buffer, 0, read);
						System.out.println("Num of bytes read= " + (totalRead += read));
					}
					log.debug("Received message body= " + body.toString());

					SensorData sensorData = XmppDAO.processMessage(body.toString());
					if (sensorData == null) {
						throw new IOException("Could not parse data");
					}
					log.info("Data successfully parsed");

					int idleTime = 0;
					for (Data data : sensorData.getData()) {
						if (data.getType() == Data.TYPE_TEMPERATURE) {
							idleTime = engine.calculatePredictTime(data.getValue());
						}
					}

					log.debug("Request handled in " + (System.currentTimeMillis() - startTime) + " ms");
					response.setContentType("application/json;charset=utf-8");
					response.setStatus(HttpServletResponse.SC_OK);
					String respString = String.format("{\"idle\": %d}", idleTime);
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
}