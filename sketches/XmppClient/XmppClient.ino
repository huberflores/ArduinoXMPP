#include <SPI.h>
#include <Ethernet.h>
#include <Xmpp.h>
#include <TinkerKit.h>
#include <SensorProtocol.h>

byte mac[] = { 
  0x90, 0xA2, 0XDA, 0X00, 0XF8, 0X06 };
//IPAddress server(192,168,1,91);
char *server = "ec2-23-22-34-73.compute-1.amazonaws.com";
EthernetClient client;

char* recipient = "arduinoserver@amazon-xmpp";
XMPP xmpp("arduino", "arduino", "sensor", "amazon-xmpp", recipient);

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
  // start the serial library:
  Serial.begin(9600);
  Serial.println("Serial started...");
  xmpp.setSerial(&Serial);
  xmpp.setClient(&client);
  protocol.setSensors(sensorTypes, length);

  // start the Ethernet connection:
  Ethernet.begin(mac);
  // give the Ethernet shield a second to initialize:
  delay(1000);
  getConnectedWithServer();
}

void getConnectedWithServer() {
  Serial.println("connecting...");
  // if you get a connection, report back via serial
  while (!client.connect(server, 5222)) {
    // if you didn't get a connection to the server, report it and try again in a second
    Serial.println("Connection failed, retrying in 10, seconds");
    delay(10000);
    Serial.println("Retrying");
    client.flush();
  } 
  // let propely initialize the connection
  delay(2000);
  red.off();
  yellow.on();
  Serial.println("connected");

  getConnectedWithXMPP();
}

void getConnectedWithXMPP(){
  while(!xmpp.connect()){
    Serial1.println("XMPP Stream negotiation failed");
    delay(10000);
    Serial.println("Retrying..");
  }
  Serial.println("Xmpp connection established");

  yellow.off();   
  green.on(); 
}
void loop(){
  if(!client.connected()) {
    Serial.println("Lost connection to server");
    sendData = false;
    xmpp.releaseConnection();
    green.off();
    yellow.off();
    red.on();
    getConnectedWithServer();
  } 
  else if(!xmpp.getConnected()) {
    Serial.println("XML Stream closed");
    sendData = false;
    green.off();
    yellow.on();
    getConnectedWithXMPP();  
  } 
  else {
    xmpp.handleIncoming(); 
    sendData = xmpp.getRecAvailable();
  }

  //if(sendData && millis() - currentTime > (reportStep*1000)){
  if(sendData){
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
  delay(reportStep*1000);
}

void flicker(){
  blue.on();
  delay(50);
  blue.off(); 
}
