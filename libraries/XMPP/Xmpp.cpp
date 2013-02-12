#include <Xmpp.h>
#include <avr/pgmspace.h>
#include <Base64.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <Utils.h>
#include <Arduino.h>

prog_char open_stream[] PROGMEM = "<?xml version='1.0' encoding='UTF-8'?>"
	"<stream:stream to='%s' xmlns:stream='http://etherx.jabber.org/streams' "
	"xmlns='jabber:client' xml:lang='en' version='1.0'>";
	
prog_char open_stream_expected[] PROGMEM = "</stream:features>";

prog_char auth_stream[] PROGMEM = "<auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' "
	"mechanism='PLAIN'>%s</auth>";

prog_char compress_stream[] PROGMEM = "<compression xmlns='http://jabber.org/features/compress'>"
	"<method>%s</method>"
	"</compression>";
	
const prog_char bind_resource_init[] PROGMEM = "<bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/>";

prog_char bind_resource[] PROGMEM = "<bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'>" 
	"<resource>%s</resource></bind>";

prog_char iq_stanza_short[] PROGMEM = "<iq to='%s' type='%s' from='%s' id='%s'/>";

const prog_char iq_stanza[] PROGMEM = "<iq type='%s' id='%s'>%s</iq>";

prog_char presence_stanza[] PROGMEM = "<presence from='%s' xml:lang='en'>"
	"<status>%s</status>"
	"<show>%s</show>"
	"</presence>";
	
prog_char roster[] PROGMEM = "<query xmlns='jabber:iq:roster'/>";

prog_char message_stanza[] PROGMEM = "<message from='%s' to='%s' type='%s' xml:lang='en'>"
	"<body>%s</body></message>";

prog_char close_stream[] PROGMEM = "<presence from='%s' type='unavailable'/></stream:stream>";


XMPP::XMPP(char* username, char* password, char* resource, char* server, char* recipient) : bufStep(50) {
	this->username = username;
	this->password = password;
	this->resource = resource;
	this->server = server;
	this->recipient = recipient;

	int jidLength = strlen(this->username) + strlen(this->server) + 1;
	this->jid = (char*) malloc(jidLength + 1);
	sprintf(this->jid, "%s@%s", username, server);
	
	this->currentState = CLOSED;
	this->connected = false;
	this->currSize = this->bufStep;
}

bool XMPP::connect() {
	this->buffer = (char*) malloc(currSize);
	bool sendMessage = true;
	uint16_t currentLetter = 0;
	while(true){
		if(sendMessage){
			sendStanza();
			sendMessage = false;
		}
		while(this->client->available() > 0){
			char letter = (char)this->client->read();
			if(currentLetter + 5 >= currSize) {
				resizeBuffer();
			}
			buffer[currentLetter++] = letter;
			buffer[currentLetter] = '\0';
			if(!this->client->available() && checkIfComplete(buffer)) {
				debug("Incoming", buffer);
				currentLetter = 0;
				sendMessage = processInput(buffer);
				flushBuffer();
			}
		}
		if(getConnected()) {
			return true;
		}
	}
	return false;
}


void XMPP::handleIncoming() {
	uint16_t currentLetter = 0;
	unsigned long currentMillis = millis();
	bool dataReceived = false;
	bool dataComplete = false;
	do {
		while(this->client->available() > 0) {
			dataReceived = true;
			char letter = (char)this->client->read();
			if(currentLetter + 5 >= currSize) {
				resizeBuffer();
			}
			buffer[currentLetter++] = letter;
			buffer[currentLetter] = '\0';
		}
		if(dataReceived && checkIfComplete(buffer)) {
			debug("Complete message received");
			dataComplete = true;
			break;
		}
	} while(millis() - currentMillis < 2000  && !dataComplete && dataReceived);	
	
	if(dataReceived){
		debug("Incoming", buffer);
		currentLetter = 0;
		processInput(buffer);
		flushBuffer();
	}
}

bool XMPP::checkIfComplete(char* buffer) {
	debug("checkIfComplete()");
	bool success = false;
	switch (this->currentState) {
		case CLOSED:
		case AUTH:
			success = checkIfComplete(buffer, open_stream_expected);
			break;
		case OPEN:
			success = Utils::endsWith(buffer, ">");
			break;
		case AUTH_OPEN:
			success = Utils::endsWith(buffer, "</iq>");
			break;
		case AVAILABLE:
		case SENDING:
			success = checkMessage(buffer);
			break;
	}
	return success;
}

bool XMPP::checkIfComplete(char* buffer, const prog_char *expected_P) {
	int length = strlen_P(expected_P);
	char expected[length + 1];
	strcpy_P(expected, expected_P);
	return Utils::endsWith(buffer, expected);
}

void XMPP::sendStanza() {
	switch (this->currentState) {
		case CLOSED:
		case AUTH:
			sendOpenStream();
			break;
		case OPEN:
			authenticate();
			break;
		case AUTH_OPEN:
			bindResource();
			break;
		case BIND:
			createPresence("Sending data", "chat");
			break;
	}
}

void XMPP::setSerial(Stream* stream) {
	this->serial = stream;
}

void XMPP::sendOpenStream() {
	debug("sendOpenStream()");
	int totalStreamLen = strlen_P(open_stream) + strlen(this->server) - 1;
	char buffer[totalStreamLen];
	sprintf_P(buffer, open_stream, this->server);
	send(buffer);
}

void XMPP::authenticate() {
	debug("authenticate()");
	int plainAuthTokenLen = strlen(this->username) + strlen(this->password) + 3;
	int base64TokenLen = base64_enc_len(plainAuthTokenLen);
	char plainAuthToken[plainAuthTokenLen];
	char base64Token[base64TokenLen];
	
	memset(plainAuthToken, '\0', plainAuthTokenLen);
	memcpy(plainAuthToken + 1, this->username, strlen(this->username));
	memcpy(plainAuthToken + strlen(this->username) + 2, this->password, strlen(this->password));
	base64_encode(base64Token, plainAuthToken, plainAuthTokenLen);
	
	int authLen = strlen_P(auth_stream) + base64TokenLen;
	char buffer[authLen];
	sprintf_P(buffer, auth_stream, base64Token);
	send(buffer);
}

void XMPP::bindResource(){
	char* type = "set";
	debug("bindResource()");
	// Replace the specifier(%s) in bind template with correct value
	int bindResourceLength = strlen_P(bind_resource) + strlen(this->resource);
	char bindTemp[bindResourceLength];
	sprintf_P(bindTemp, bind_resource, this->resource);
	
	char* uniqueID = createUniqueID();
	int iqLength = strlen_P(iq_stanza) + strlen(type) + strlen(uniqueID);
	
	int totalBindResourceLength = strlen(bindTemp) + iqLength;
	char buffer[totalBindResourceLength];
	int n = sprintf_P(buffer, iq_stanza, type, uniqueID, bindTemp);
	send(buffer);
}

void XMPP::createPresence(char* status, char* message){
	debug("createPresence()");
	int totalPresenceLength = strlen_P(presence_stanza) + strlen(status) + strlen(message) + strlen(this->jid) - 5;
	char buffer[totalPresenceLength];
	
	sprintf_P(buffer, presence_stanza, this->jid, status, message);
	this->connected = true;
	this->currentState = AVAILABLE;
	send(buffer);
}

void XMPP::createRoster(){
	debug("createRoster()");
	int rosterLength = strlen_P(roster);
	char rosterTemp[rosterLength];
	strcpy_P(rosterTemp, roster);
	char* type = "get";
	char* uniqueID = createUniqueID();
	int iqLength = strlen(rosterTemp) + strlen_P(iq_stanza) + strlen(type) + strlen(uniqueID);
	char buffer[iqLength];
	int n = sprintf_P(buffer, iq_stanza, type, uniqueID, rosterTemp);
	send(buffer);
}


void XMPP::sendMessage(char* to, char* body, char* type){
	int bodyLength = strlen(body);
	char bodyData[bodyLength + 1];
	memset(bodyData, '\0', bodyLength + 1);
	memcpy(bodyData, body, bodyLength);
	int messageLength = strlen_P(message_stanza) + 1;
	char templ[messageLength];
	strcpy_P(templ, message_stanza);
	int totalMessageLength = messageLength + strlen(this->fullJid) + strlen(to) + bodyLength + strlen(type) + 1;
	char buffer[totalMessageLength];
	int n = sprintf(buffer, templ, this->jid, to, type, bodyData);
	send(buffer);
}

void XMPP::closeStream(){
	debug("closeStream");
	int closeStreamLen = strlen_P(close_stream);
	char temp[closeStreamLen];
	strcpy_P(temp, close_stream);
	int totalCloseStreamLen = strlen_P(close_stream) + strlen(this->fullJid);
	char buffer[totalCloseStreamLen];
	sprintf(buffer, temp, this->fullJid);
	this->currentState = CLOSED;
	this->connected = false;
	send(buffer);
}

char* XMPP::createUniqueID() {
	int idLength = 8;
	char uniqueId[idLength];
	for (int i = 0; i < idLength; ++i) {
         int randomChar = rand() % (26 + 26 + 10);
         if (randomChar < 26)
             uniqueId[i] = 'a' + randomChar;
         else if (randomChar < 26 + 26)
             uniqueId[i] = 'A' + randomChar - 26;
         else
             uniqueId[i] = '0' + randomChar - 26 - 26;
     }
    uniqueId[idLength] = 0;
	return uniqueId;
}

bool XMPP::processInput(char* input) {
	debug("processInput");
	String stanza = String(input);
	bool sendMessage = false;
	switch (this->currentState) {
		case CLOSED:
		case AUTH:
			sendMessage = handleOpenStream(stanza);
			break;
		case OPEN:
			sendMessage = handleAuthenticate(stanza);
			break;
		case AUTH_OPEN:
			sendMessage = handleBindResource(stanza);
			break;
		case AVAILABLE:
		case SENDING:
			handleStanza(input);
		break;
	}
	return sendMessage;
}


bool XMPP::handleOpenStream(String input) {
	debug("handleOpenStream()");
	if(input.indexOf("?xml") > -1){
		input = input.substring(input.indexOf("<", 1), input.length() + 1);
	}
	if(input.indexOf("stream:features") > -1) {
		// TODO handle stream features
		if(this->currentState == CLOSED) {
			this->currentState = OPEN;
		} else if (this->currentState == AUTH) {
			this->currentState = AUTH_OPEN;
		}
		return true;
	}
	return false;
}

bool XMPP::handleAuthenticate(String input){
	debug("handleAuthenticate()");
	if(input.indexOf("success") > -1){
		debug("AUTH", "Successfully authenticated");
		this->currentState = AUTH;
		return true;
	}
	debug("AUTH", "Authentication unsuccessful");
	return false;
}

bool XMPP::handleBindResource(String input){
	String jidValue = findTagBody(input, "jid");
	bool success = true;
	if(jidValue.length() > 0) {
		this->fullJid = (char*) malloc(jidValue.length() + 1);
		jidValue.toCharArray(this->fullJid, jidValue.length() + 1);
	} else {
		int jidLength = strlen(this->jid) + strlen(this->resource) + 1;	
		this->fullJid = (char*) malloc(jidLength + 1);
		sprintf(this->fullJid, "%s/%s", this->jid, this->resource);	
	}		
	this->currentState = BIND;
	return success;
}

String XMPP::findTagBody(String input, String tagName) {
	int startingBracket;
	int closingBracket;
	int space;
	int currentIndex = 0;
	String tag;
	String value = "";
	while(startingBracket > -1) {
		startingBracket = input.indexOf("<", currentIndex);
		closingBracket = input.indexOf(" ", startingBracket);
		if(closingBracket == -1 || closingBracket > input.indexOf(">", startingBracket)) {
			closingBracket = input.indexOf(">", startingBracket);
		}
		tag = input.substring(startingBracket + 1, closingBracket);
		if(tag.equals(tagName)) {
			value = input.substring(closingBracket + 1, input.indexOf("<", closingBracket));
			break;
		} else {
			currentIndex = startingBracket + 1;
		}
	}	
	return value;
}

String XMPP::findAttrValue(String input, String attrName) {
	int attrIndex = input.indexOf(attrName);
	int attrLength = attrName.length() + 2;
	if(attrIndex > -1) {
		int doubleQuote = input.indexOf("\"", attrIndex + attrLength);
		int singleQuote = input.indexOf("'", attrIndex + attrLength);
		int quoteIndex = (doubleQuote > -1 && doubleQuote < singleQuote || singleQuote == -1) ?  doubleQuote : singleQuote;
		String attrValue = input.substring(attrIndex + attrLength, quoteIndex);
		return attrValue;
	} else {
		return "";
	}
}

void XMPP::handleStanza(char* input) {
	String inStanza = String(input);
	String stanza = inStanza.substring(1, inStanza.indexOf(" ", 1));
	if(inStanza.indexOf("</stream:stream>") > -1) {
		this->connected = false;
		this->currentState = CLOSED;
		debug("Stream closed by server");
	} else if(stanza.startsWith("iq")) {
		handleIQ(String(input));
	} else if(stanza.startsWith("message")) {
		handleMessage(String(input));
	} else if(stanza.startsWith("presence")) {
		handlePresence(String(input));
	}
}

void XMPP::handlePresence(String input) {
	debug("handlePresence");
	String type = findAttrValue(input, "type");
	if(type.length() > 0) {
		if(type.equals("subscribe")) {
			// TODO: handle
		} else if(type.equals("unavailable")) {
			String from = findAttrValue(input, "from");
			if(from.startsWith(this->recipient)) {
				this->currentState = AVAILABLE;
			}
		}
	} else {
		String status = findTagBody(input, "show");
		String from = findAttrValue(input, "from");
		if(from.startsWith(this->recipient)) {
			debug("Current state: SENDIND");
			this->currentState = SENDING;
		}
	}
}

void XMPP::handleMessage(String input) {
	int bodyStart = input.indexOf("body") + 6;
	int bodyEnd = input.substring(bodyStart).indexOf("body") - 2;
	String messageBody = input.substring(bodyStart, bodyEnd);
	char buffer[messageBody.length() + 1];
	messageBody.toCharArray(buffer, messageBody.length() + 1);
	debug("Message Received:", buffer);
}

void XMPP::handleIQ(String input) {
	int firstClosingBracket = input.indexOf(">");
	int lastOpeningBracket = input.lastIndexOf("<");
	String content = input.substring(firstClosingBracket + 1, lastOpeningBracket);
	String method = content.substring(1, content.indexOf(" ", 1));
	if(method.startsWith("ping")) {
		int idStart = input.indexOf("id") + 4;
		int idEnd = input.indexOf("\"", idStart);
		String id = input.substring(idStart, idEnd);
		char buffer[id.length() + 1];
		id.toCharArray(buffer, id.length() + 1);
		handlePing(buffer);
	}
}

void XMPP::handlePing(char* id) {
	char type[] = "result";
	int iqTempLen = strlen_P(iq_stanza_short) + strlen(id) + strlen(this->server) + strlen(this->fullJid) + strlen(type) - 7;
	char buffer[iqTempLen];
	sprintf_P(buffer, iq_stanza_short, this->server, type, this->fullJid, id);
	send(buffer);
}

bool XMPP::getConnected(){
	return this->connected;
}

void XMPP::setClient(Stream* client) {
	this->client = client;
}

Stream* XMPP::getClient(){
	return this->client;
}

void XMPP::flushBuffer(){
	free(buffer);
	currSize = 50;
	buffer = (char*) malloc(currSize);
}

void XMPP::resizeBuffer() {
	int newSize = currSize + bufStep;
	char* newBuf = (char*) malloc(newSize);
	memcpy(newBuf, buffer, currSize);
	currSize = newSize;
	free(buffer);
	buffer = newBuf;  
}

void XMPP::debug(char* message) {
	if(this->serial != NULL){
		this->serial->print("D ");
		this->serial->println(message);
		this->serial->flush();
	}
}

void XMPP::debug(char* intro, char* message) {
	if(this->serial != NULL){
		this->serial->print("D ");
		this->serial->print(intro);
		this->serial->print(" ");
		this->serial->println(message);
		this->serial->flush();
	}
}

void XMPP::setRecipient(char* recipient) {
	this->recipient = recipient;
}

bool XMPP::getRecAvailable() {
	return this->currentState == SENDING;
}

void XMPP::send(char* data) {
	debug("Sending", data);
	this->client->print(data);
}

bool XMPP::checkMessage(char* buffer) {
	uint8_t tags = 0;
	for(int i = 0; ;i++) {
		if(buffer[i] == '\0'){
			if(i == 0)
				return false;			
			break;
		}
		if(buffer[i] == '<') {
			tags++;
		} else if(buffer[i] == '>') {
			tags--;
		}
	}	
	return tags == 0;
}

void XMPP::releaseConnection(){
	this->currentState = CLOSED;
}