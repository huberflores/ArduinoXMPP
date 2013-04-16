package ee.ut.ati.masters;

import ee.ut.ati.masters.http.ArduinoHandler;
import ee.ut.ati.masters.xmpp.XmppProperties;
import ee.ut.ati.masters.xmpp.client.XmppManager;
import org.apache.commons.math.stat.regression.GLSMultipleLinearRegression;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;

import java.util.Arrays;

public class Main {

	static double[] params;
	static double[][] values;
	static double[] samples;

	public static void main(String[] args) throws Exception {

		//Start the web server
		Server server = new Server(8080);
		server.setHandler(new ArduinoHandler());
		server.start();
		server.join();


//		// Reads in Openfire servers serverside clients address, username and password
//		XmppProperties.readProperties();
//
//		// Makes a XmppManager object using port 5222 and data that came from XmppProperties class
//		XmppManager xmppManager = new XmppManager(XmppProperties.serverAddress,
//				5222, XmppProperties.user, XmppProperties.password);
//
//		// Makes and starts the XmppManager thread
//		Thread xmpp = new Thread(xmppManager);
//		xmpp.start();
//
//
//		// Endless cycle for some reason??????????????
//		while (true)
		//debug();

	}

	private static void debug() throws Exception {
		Logger log = Logger.getLogger("Main");
//
//		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
//		double[] y = new double[] {9.64, 1, 9.50, 10, 9.55, 15, 9.40, 20, 9.30, 30, 9.30, 40};
		//samples = y;
		//double[][] x = new double[6][];
//		x[0] = new double[] {0, 0, 0, 0, 0};
//		x[1] = new double[] {2.0, 0, 0, 0, 0};
//		x[2] = new double[] {0, 3.0, 0, 0, 0};
//		x[3] = new double[] {0, 0, 4.0, 0, 0};
//		x[4] = new double[] {0, 0, 0, 5.0, 0};
//		x[5] = new double[] {0, 0, 0, 0, 6.0};

		//x[0] = new double[] {2, 3, 4, 5, 6}
//		regression.newSampleData(y, 6, 1);
//		params = regression.estimateRegressionParameters();
//		for (int i = 0; i < params.length; i++) {
//			System.out.println(params[i]);
//		}
//
//		log.debug("beta: " + Arrays.toString(regression.estimateRegressionParameters()));
//
//		log.debug("residuals: " + Arrays.toString(regression.estimateResiduals()));
//
//		log.debug("paramVariance: " + Arrays.deepToString(regression.estimateRegressionParametersVariance()));
//
//		log.debug("errorVariance: " + regression.estimateErrorVariance());
//
//		log.debug("regressandVariance: " + regression.estimateRegressandVariance());
//
//		log.debug("rSquared: " + regression.calculateRSquared());
//
//		log.debug("sigma: " + regression.estimateRegressionStandardError());
//
//		log.debug("regression std: " + regression.estimateRegressionStandardError());


//		double x6 = params[0];
		//double x6 = 0;
		//for (int i = 1; i < 6; i++) {
		//	x6 += params[i] * x[6 - i][0];
		//}

		//log.debug("x6 = " + x6);
		samples = new double[]{1, 10, 15, 20, 30, 40};
		SimpleRegression simpleReg = new SimpleRegression();
		simpleReg.addData(new double[][]{{1, 9.64}, {10, 9.50}, {15, 9.55}, {20, 9.40}, {30, 9.30}, {40, 9.30}});
		log.debug(simpleReg.getInterceptStdErr());
		log.debug(simpleReg.getSlopeConfidenceInterval());
		log.debug(simpleReg.getMeanSquareError());
		log.debug(simpleReg.getR());
		log.debug(simpleReg.getRSquare());
		log.debug(simpleReg.getSignificance());
		log.debug("simple regression " + simpleReg.predict(50));
		log.debug("multi regression " + predict(10));
	}

	public static double predict(int k) {
		Logger log = Logger.getLogger("Prediction");
		int p = 1;
		int N = samples.length;
		double e = 0;
		if (k < 0) {
			return samples[N+k-1];
		} else {
			for (int i=0; i < p; i++) {
				double prediction = predict(k - 1);
				//log.info((k - 1) + " prediction value is " + prediction);

				e += params[1] * prediction;
				//log.info("e value after prediction added = " + e);
				//log.info(k + " value is " + (e + params[0]));

			}
		}
		e += params[0];

		return e;
	}
}
