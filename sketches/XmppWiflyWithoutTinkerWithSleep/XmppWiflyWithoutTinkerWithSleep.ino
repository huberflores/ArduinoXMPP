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

void (*printD)(char* string);

void print(char* string) {
  Serial.println(string);
}

void print1(char* string) {
  Serial1.println(string);
}

void setup(){
  printD = print1;  
  Serial.begin(9600);
  Serial1.begin(9600);  
  Serial1.println("Serial1 started");
  xmpp.setSerial(&Serial1);
  WiFly.setUart(&Serial);
  WiFly.begin();
  WiFly.configure(WAKE_TIMER, 0);
  WiFly.configure(SLEEP_TIMER, 0);
  WiFly.configure(WLAN_JOIN, 1);
  WiFly.configure(COMM_SIZE, 1420);
  xmpp.setClient(&client);
  protocol.setSensors(sensorTypes, length);
  getConnected();
  getConnectedWithServer();
}

void getConnected() {
  WiFly.join("poku", "pokunett");
  /*
  while(!WiFly.join("poku", "pokunett")){
    printD("Join fail");
    delay(1000);
  }*/
}

void getConnectedWithServer() {
  while(!client.connect()){
    printD("TCP Connection failed");
    delay(1000);
  }
  // let propely initialize the connection
  delay(2000);
  printD("connected");

  getConnectedWithXMPP();
}

void getConnectedWithXMPP(){
  if(!xmpp.connect()){
    Serial1.println("AAAAAAA AAAAAAAAAAA AAAAAAAAAAAAA XMPP Stream negotiation failed");
    client.flush();
    client.stop();
    return;
  }
  printD("Xmpp connection established");
}

void loop() {
  //WiFly.configure(SLEEP_TIMER, 0);
  //Serial.println("\nloop");
  Serial1.print("CurrentState = ");
  Serial1.print(xmpp.getState());
  if(!client.connected()) {
    printD("Client not connected");
    sendData = false;
    xmpp.releaseConnection();
    // reconnect
    getConnectedWithServer();
  } 
  else if(!xmpp.getConnected()) {
    printD("XMPP not connected");
    sendData = false;
    // reconnect
    getConnectedWithXMPP();  
  } 
  else {
    Serial1.print("All connections active");
    xmpp.handleIncoming(); 
    sendData = xmpp.getRecAvailable();   
  }
  
  //xmpp.handleIncoming(); 
  //sendData = xmpp.getRecAvailable();

  if(sendData) {
  //if(sendData && millis() - currentTime > (reportStep*1000)){
    float tm = 1.0f;
    float hs = 1.0f;
    float ldr = 1.0f;
    protocol.addValue(1, tm);
    protocol.addValue(2, hs);
    protocol.addValue(3, ldr);
    char* message = protocol.createMessage();
    xmpp.sendMessage(recipient, message, "chat");
    /*delay(1000);
    xmpp.closeStream();
    xmpp.releaseConnection();
    client.flush();
    client.stop();
    //WiFly.configure(WAKE_TIMER, 30);
    WiFly.configure(SLEEP_TIMER, 2);
    WiFly.configure(SLEEP, 0);
    */
    //Sleepy::loseSomeTime(reportStep * 1000);   
    WiFly.configure(SLEEP, 0);
    Sleepy::loseSomeTime(65300);
  } else {
    printD("Send data is false");
    //Sleepy::loseSomeTime(1000);
  }
}
