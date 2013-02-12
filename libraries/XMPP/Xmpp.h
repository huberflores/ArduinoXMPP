#include <Stream.h>
#include <Print.h>

enum XmppState {
	CLOSED,
	OPEN,
	AUTH,
	AUTH_OPEN,
	BIND,
	AVAILABLE,
	SENDING
};

class XMPP {
private:
	char* username;
	char* password;
	char* resource;
	char* server;
	char* jid;
	char* fullJid;
	char* buffer;
	uint16_t currSize;
	const uint8_t bufStep;
	Stream* serial;
	XmppState currentState;
	bool connected;
	Stream* client;
	bool sendData;
	char* recipient;

	bool handleOpenStream(String input);
	bool handleAuthenticate(String input);
	bool handleBindResource(String input);
	
	void sendOpenStream();
	void authenticate();
	void bindResource();
	void createPresence(char* status, char* message);
	void createRoster();
	
	void handlePresence(String input);
	void handleMessage(String input);
	void handleIQ(String input);
	void handlePing(char* id);
	
	bool processInput(char* input);
	void handleStanza(char* input);
	void sendStanza();
	void send(char* data);
	bool checkIfComplete(char* buffer);
	bool checkIfComplete(char* buffer, const prog_char *expected_P);
	bool checkMessage(char* buffer);
	

	char* createUniqueID();
	String findTagBody(String input, String tagName);
	String findAttrValue(String input, String attrName);
	void debug(char* message);
	void debug(char* intro, char* message);
	
	void flushBuffer();
	void resizeBuffer();

public:
	XMPP(char* username, char* password, char* resource, char* server, char* recipient);
	
	bool connect();
	void sendMessage(char* to, char* body, char* type);
	void handleIncoming();
	void closeStream();
	bool getRecAvailable();
	
	void setClient(Stream* client);
	void setSerial(Stream* stream);
	void setRecipient(char* recipient);
	bool getConnected();
	Stream* getClient();
	
	void releaseConnection();
};