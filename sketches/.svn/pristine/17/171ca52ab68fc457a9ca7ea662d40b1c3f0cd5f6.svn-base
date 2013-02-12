#include "WiFly.h"
#include <Xmpp.h>


XMPP xmpp("arduino", "arduinomega", "sensor", "arduino-xmpp");
int retries = 5;
int retryCounter = 0;
char *buffer;
int currentLetter = 0;
int currSize = 50;
int bufStep = 50;

bool sendMessage = false;

unsigned long reportStep = 10000;
unsigned long currentTime;
char* serverAddress = "192.168.1.77";
int serverPort = 5222;
//char* serverAddress = "192.0.3.166";

WiFlyClient client(serverAddress, serverPort);

void setup(){  
  Serial.begin(9600);
  Serial.println("Start Setup");
  //  xmpp.setSerial(&Serial);
  WiFly.setUart(&Serial);
  WiFly.begin();

  if (!WiFly.join()) {
    Serial.println("Join fail");  
    while (1) {
      // Hang on failure.
    }
  }
  if(client.connect()){
    sendMessage = true;
    buffer = (char*) malloc(currSize);
  } 
  else {
    // Hang on failure. 
  }
}

void loop()
{
  // if the server's disconnected, stop the client:
  if (!client.connected() || retryCounter == retries) {
    client.stop();    
    // do nothing:
    while(true); 
  }

  if(xmpp.getConnected()){
    while(client.available() > 0){
      char letter = (char)client.read();
      if(currentLetter >= currSize) {
        resizeBuffer();
      }
      buffer[currentLetter] = letter;
      currentLetter++;
      if(!client.available()){
        buffer[currentLetter] = '\0';
        currentLetter = 0;
        char* stanza = xmpp.handleStanza(buffer);
        flushBuffer();
        if(strlen(stanza) > 0){
          client.print(stanza);
        } 
      }
    }
    delay(1000);
  }   
  else {
    while(client.available() > 0){
      char letter = (char)client.read();
      if(currentLetter + 5 >= currSize) {
        resizeBuffer();
      }
      buffer[currentLetter] = letter;
      currentLetter++;
      if(!client.available()){
        buffer[currentLetter] = '\0';
        currentLetter = 0;
        sendMessage = xmpp.processInput(buffer);
        flushBuffer();
        if(!sendMessage) {
          retryCounter++;
        }
      }
    }
    if(sendMessage){
      char* stanza = xmpp.createStanza();
      client.print(stanza);
      sendMessage = false;
    }
    if(xmpp.getConnected()){
      currentTime = millis();
    }
    delay(1000);
  }
}

void flicker(){
  blue.on();
  delay(50);
  blue.off(); 
}

void flushBuffer(){
  free(buffer);
  currSize = 50;
  buffer = (char*) malloc(currSize);
}

void resizeBuffer() {
  int newSize = currSize + bufStep;
  char* newBuf = (char*) malloc(newSize);
  memcpy(newBuf, buffer, currSize);
  currSize = newSize;
  free(buffer);
  buffer = newBuf;  
}


bool startsWith(char* buffer, char* item) {
  if(strlen(item) > strlen(buffer)) {
    return false; 
  }
  for(int i = 0; ;i++) {
    if(item[i] == '\0') {
      break;
    }
    if(item[i] != buffer[i]){
      return false; 
    }
  }
  return true;
}

bool endsWith(char* buffer, char* item) {
  if(strlen(item) > strlen(buffer)) {
    return false;
  } 
  for(int i = strlen(item) - 1, j = strlen(buffer) - 1;i > 0; i--,j--){
    if(item[i] != buffer[j]){
      return false;
    }
  }
  return true;
}




