package ee.ut.ati.masters.xmpp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class XmppProperties {
	private static Logger logger = Logger.getLogger(XmppProperties.class);

	public static String serverAddress;	
	public static String user;
	public static String password;

	/*
	 * Method that reads data from xmpp.properties file. That data should include Openfire server 
	 * serverside client username, password and address
	 */
	public static void readProperties() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("xmpp.properties"));
			serverAddress = properties.get("server").toString();
			user = properties.get("user").toString();
			password = properties.get("password").toString();
			logger.info("Properties file read");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
