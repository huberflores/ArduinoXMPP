package ee.ut.ati.masters.xmpp.client;

import ee.ut.ati.masters.xmpp.XmppProperties;

public class XmppMain {

	public static void main(String[] args) throws Exception {
		/*
		 * Reads in Openfire servers serverside clients address, username and password
		 */
		XmppProperties.readProperties();
		
		/*
		 *  Makes a XmppManager object using port 5222 and data that came from XmppProperties class
		 */
		XmppManager xmppManager = new XmppManager(XmppProperties.serverAddress,
				5222, XmppProperties.user, XmppProperties.password);
		
		/*
		 * Makes and starts the XmppManager thread
		 */
		Thread xmpp = new Thread(xmppManager);
		xmpp.start();

		/*
		 * Endless cycle for some reason??????????????
		 */
		while (true)
			;
	}

}
