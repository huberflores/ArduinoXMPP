package ee.ut.ati.masters.xmpp.client;

import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

import ee.ut.ati.masters.h2.ConnectionFactory;
import ee.ut.ati.masters.h2.XmppDAO;
import ee.ut.ati.masters.h2.data.Data;
import ee.ut.ati.masters.h2.data.SensorData;

public class XmppManager implements Runnable {

	private static final int packetReplyTimeout = 500; // millis
	private static Logger logger = Logger.getLogger(XmppManager.class);

	private String server;
	private int port;
	private String username;
	private String password;
	private ObjectMapper mapper;

	private ConnectionConfiguration config;
	private XMPPConnection connection;

	private ChatManager chatManager;
	private MessageListener messageListener;
	private ChatManagerListener chatManageListener;

	public XmppManager(String server, int port, String username, String password) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.password = password;
		this.mapper = new ObjectMapper();
	}

	public void run() {
		try {
			logger.info(String.format(
					"Initializing connection to server %1$s port %2$d", server,
					port));

			/*
			 * Setting the period of time to wait for a replay from server
			 */
			SmackConfiguration.setPacketReplyTimeout(packetReplyTimeout);
			/*
			 * Setting up configurations to use while establishing the connection with server
			 */
			config = new ConnectionConfiguration(server, port);
			config.setSASLAuthenticationEnabled(true);
			config.setSecurityMode(SecurityMode.disabled);
			/*
			 * Creates a socket connection to a XMPP server
			 */
			connection = new XMPPConnection(config);
			connection.connect();

			// If connected to the XMPP server
			if (connection.isConnected()) {
				logger.info("Connection successfully established");

				chatManager = connection.getChatManager();
				chatManageListener = new ArduinoChatManagerListener();
				chatManager.addChatListener(chatManageListener);
				messageListener = new ArduinoMessageListener();

				performLogin();
				Roster roster = connection.getRoster();
				roster.addRosterListener(new ArduinoRosterListener());
				setStatus(true, "Receiving data");
				
			// If not connected to the XMPP server
			} else {
				logger.info("Connection failed");
			}
		} catch (XMPPException e) {
			logger.warn("Xmpp connection failed", e);
		}
	}

	public void beginChat(String buddyJID) {
		logger.info(String.format("Starting chat with user %s", buddyJID));
		chatManager.createChat(buddyJID, messageListener);
	}

	public void performLogin() throws XMPPException {
		if (connection != null && connection.isConnected()) {
			connection.login(username, password);
		}
	}

	public void setStatus(boolean available, String status) {
		Presence.Type type = available ? Type.available : Type.unavailable;
		Presence presence = new Presence(type);

		presence.setStatus(status);
		presence.setMode(Mode.available);
		connection.sendPacket(presence);
	}

	public void destroy() {
		if (connection != null && connection.isConnected()) {
			connection.disconnect();
		}
	}

	public void printRoster() throws Exception {
		Roster roster = connection.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();
		for (RosterEntry entry : entries) {
			logger.debug(String.format("Buddy:%1$s - Status:%2$s",
					entry.getName(), entry.getStatus()));
		}
	}

	public void sendMessage(String message, String buddyJID)
			throws XMPPException {
		logger.info(String.format("Sending message '%1$s' to user %2$s",
				message, buddyJID));
		Chat chat = chatManager.createChat(buddyJID, messageListener);
		chat.sendMessage(message);
	}

	public void createEntry(String user, String name) throws Exception {
		logger.info(String.format(
				"Creating entry for buddy '%1$s' with name %2$s", user, name));
		Roster roster = connection.getRoster();
		roster.createEntry(user, name, null);
	}

	class ArduinoMessageListener implements MessageListener {

		public void processMessage(Chat chat, Message message) {
			String from = message.getFrom();
			String body = message.getBody();
			logger.info(String.format("Received message '%1$s' from %2$s",
					body, from));
			if (body.contains("SensorData")) {
				String content = body.substring(body.indexOf("{"));
				try {
					SensorData sensorData = mapper.readValue(content,
							SensorData.class);
					logger.info("Data successfully parsed");
					for (Data data : sensorData.getData()) {
						XmppDAO.insertSensorData(
								ConnectionFactory.getDataSource(),
								sensorData.getLocation(), data.getType(),
								data.getValue());
					}
				} catch (JsonParseException e) {
					logger.warn("JSON parsing failed", e);
				} catch (JsonMappingException e) {
					logger.warn("JSON mapping failed", e);
				} catch (IOException e) {
					logger.warn("Message reading failed", e);
				}
			}
		}

	}

	class ArduinoChatManagerListener implements ChatManagerListener {

		public void chatCreated(Chat chat, boolean createdLocally) {
			if (!createdLocally)
				chat.addMessageListener(messageListener);
		}

	}

	class ArduinoRosterListener implements RosterListener {

		@Override
		public void entriesAdded(Collection<String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void entriesDeleted(Collection<String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void entriesUpdated(Collection<String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void presenceChanged(Presence presence) {
			logger.info(String.format(
					"Presence changed for user: %s, type: %s ",
					presence.getFrom(), presence.getType()));
		}
	}

}
