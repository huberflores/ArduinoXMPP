#include <TinkerKit.h>
#include <Ports.h>
#include <Xmpp.h>
#include <WiFly.h>
#include <SensorProtocol.h>
#include <SPI.h>

ISR(WDT_vect) { Sleepy::watchdogEvent(); }

char* recipient = "arduinoserver@lauris";
// username, password, resource, server
XMPP xmpp("arduino","arduinomega","sensor","lauris", recipient);
char* server = "192.168.43.231";
//char* server = "ec2-23-22-34-73.compute-1.amazonaws.com";
int serverPort = 5222;
WiFlyClient client(server, serverPort);

int sensorTypes[] = { 
  1, 2, 3 };
int locationID = 1;
int length = sizeof(sensorTypes) / sizeof(int);
SensorProtocol protocol(locationID);

unsigned long reportStep = 10; // seconds
unsigned long currentTime;
bool sendData = false;

void setup(){
  Serial.begin(9600);
  Serial1.begin(9600);
  Serial1.println("Serial1 started");
  xmpp.setSerial(&Serial1);
  WiFly.setUart(&Serial);
  WiFly.begin();
  xmpp.setClient(&client);
  protocol.setSensors(sensorTypes, length);
  getConnected();
  getConnectedWithServer();
}

void getConnected() {
  while(!WiFly.join("poku", "pokunett")){
    Serial.println("Join fail");
    delay(1000);
  }
}

void getConnectedWithServer() {
  while(!client.connect()){
    Serial1.println("TCP Connection failed");
    delay(1000);
  }
  // let propely initialize the connection
  delay(2000);
  Serial1.println("connected");

  getConnectedWithXMPP();
}

void getConnectedWithXMPP(){
  while(!xmpp.connect()){
    Serial1.println("XMPP Stream negotiation failed");
    delay(1000);
    Serial1.println("Retrying..");
  }
  Serial1.println("Xmpp connection established");
}

void loop(){
  if(!client.connected()) {
    sendData = false;
    xmpp.releaseConnection();
    // reconnect
    getConnectedWithServer();
  } 
  else if(!xmpp.getConnected()) {
    sendData = false;
    // reconnect
    getConnectedWithXMPP();  
  } 
  else {
    xmpp.handleIncoming(); 
    sendData = xmpp.getRecAvailable();
  }

  if(sendData) {   
 // && millis() - currentTime > (reportStep*1000)){
    float tm = 1.0f;
    float hs = 1.0f;
    float ldr = 1.0f;
    protocol.addValue(1, tm);
    protocol.addValue(2, hs);
    protocol.addValue(3, ldr);
    char* message = protocol.createMessage();
    xmpp.sendMessage(recipient, message, "chat");
    //Sleepy::loseSomeTime(reportStep * 1000);   
  } else {
    //Sleepy::loseSomeTime(1000);
  }
  delay(reportStep * 1000);
}
