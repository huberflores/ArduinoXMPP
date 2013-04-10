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
}

void loop() {
  
  Serial.print("Loop start at ");
  Serial.println(millis());
  
  if(!client.connected()) {
    printD("Client not connected");
    sendData = false;
    // reconnect
    getConnectedWithServer();
  } 
  
  Serial.print("Connected at ");
  Serial.println(millis());
  
  client.flush();
  client.stop();
  WiFly.configure(SLEEP, 0);  
  delay(5000);
}
