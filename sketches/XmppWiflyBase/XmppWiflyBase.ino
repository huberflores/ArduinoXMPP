#include <Xmpp.h>
#include <WiFly.h>
#include <SensorProtocol.h>
#include <TinkerKit.h>
#include <SPI.h>

char* recipient = "arduinoserver@lauris";
// username, password, resource, server
XMPP xmpp("arduino","arduinomega","sensor","arduino-xmpp", recipient);
char* server = "192.168.43.231";
//char* server = "ec2-23-22-34-73.compute-1.amazonaws.com";
int serverPort = 5222;
WiFlyClient client(server, serverPort);

int sensorTypes[] = { 
  1, 2, 3 };
int locationID = 1;
int length = sizeof(sensorTypes) / sizeof(int);
SensorProtocol protocol(locationID);

TKLed red(O0);
TKLed yellow(O4);
TKLed green(O2);
TKLed blue(O3);
TKThermistor thermistor(I0);
TKHallSensor hall(I1);
TKLightSensor light(I2);

unsigned long reportStep = 10; // seconds
unsigned long currentTime;
bool sendData = false;

void setup(){
  red.on();
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
  red.off();
  yellow.on();
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
  yellow.off();   
  green.on();
}

void loop(){
  if(!client.connected()) {
    sendData = false;
    xmpp.releaseConnection();
    green.off();
    yellow.off();
    red.on();
    // reconnect
    getConnectedWithServer();
  } 
  else if(!xmpp.getConnected()) {
    sendData = false;
    green.off();
    yellow.on();
    // reconnect
    getConnectedWithXMPP();  
  } 
  else {
    xmpp.handleIncoming(); 
    sendData = xmpp.getRecAvailable();
  }

  if (sendData) {
    Serial.println("Send data is true");
  } else {
    Serial.println("Send data is false");
  }

  if(sendData) {
 // && millis() - currentTime > (reportStep*1000)){
    float tm = thermistor.getCelsius();
    float hs = hall.get();
    float ldr = light.get();
    flicker();
    currentTime = millis(); 
    protocol.addValue(1, tm);
    protocol.addValue(2, hs);
    protocol.addValue(3, ldr);
    char* message = protocol.createMessage();
    xmpp.sendMessage(recipient, message, "chat");
  }
  delay(reportStep * 1000);
}

void flicker(){
  blue.on();
  delay(50);
  blue.off(); 
}











