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

void (*printD)(char* string);

void print(char* string) {
  Serial.println(string);
}

void print1(char* string) {
  Serial1.println(string);
}

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
  WiFly.configure(WLAN_JOIN, 1);
  xmpp.setClient(&client);
  protocol.setSensors(sensorTypes, length);
  getConnected();
  getConnectedWithServer();
}

void getConnected() {
  /*while(!WiFly.join("poku", "pokunett")){
    Serial.println("Join fail");
    delay(1000);
  }*/
  WiFly.join("poku", "pokunett");
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
  if(!xmpp.connect()){
    Serial1.println("AAAAAAA AAAAAAAAAAA AAAAAAAAAAAAA XMPP Stream negotiation failed");
    client.flush();
    client.stop();
    return;
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

  if(sendData) {
    //&& millis() - currentTime > (reportStep*1000)) {
    float tm = thermistor.getCelsius();
    float hs = hall.get();
    float ldr = light.get();
    flicker(&blue);
    currentTime = millis(); 
    protocol.addValue(1, tm);
    protocol.addValue(2, hs);
    protocol.addValue(3, ldr);
    char* message = protocol.createMessage();
    xmpp.sendMessage(recipient, message, "chat");
    xmpp.closeStream();
    xmpp.releaseConnection();
    client.flush();
    client.stop();
    //WiFly.configure(WAKE_TIMER, 30);
    WiFly.configure(SLEEP, 0);
    Sleepy::loseSomeTime(reportStep * 1000);  
  } else {
    flicker(&yellow);
    //Sleepy::loseSomeTime(1000);
  } 
}

void flicker(TKLed *led) {
  led->on();
  delay(50);
  led->off(); 
}
