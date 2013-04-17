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
}
