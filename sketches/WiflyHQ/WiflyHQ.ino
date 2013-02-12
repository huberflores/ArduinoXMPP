#include <Xmpp.h>
#include <WiFlyHQ.h>
#include <SensorProtocol.h>
#include <TinkerKit.h>

char* recipient = "arduinoserver@amazon-xmpp";
XMPP xmpp("arduino","arduino","sensor","amazon-xmpp", recipient);
//char* server = "192.168.1.67";
char* server = "ec2-23-22-34-73.compute-1.amazonaws.com";
int serverPort = 5222;

WiFly wifly;

/* Change these to match your WiFi network */
const char mySSID[] = "ThomsonF218A5";
const char myPassword[] = "DAE2073CA5";

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
  xmpp.setClient(&wifly);
  protocol.setSensors(sensorTypes, length);
  getConnected();
  getConnectedWithServer();
}

void getConnected() {
  while(!wifly.begin(&Serial, &Serial1)) {
    Serial1.println("Failed to start wifly");
    delay(1000);
  }

  /* Join wifi network if not already associated */
  if (!wifly.isAssociated()) {
    /* Setup the WiFly to connect to a wifi network */
    Serial.println("Joining network");
    wifly.setSSID(mySSID);
    wifly.setPassphrase(myPassword);
    wifly.enableDHCP();

    while(!wifly.join()) {
      Serial1.println("Join failed");
    } 
    Serial1.println("Joined wifi network");
  } 
  else {
    Serial.println("Already joined network");
  }

  Serial1.println("WiFly ready");

  wifly.setDeviceID("Wifly-TCP");
  wifly.setIpProtocol(WIFLY_PROTOCOL_TCP);

  if (wifly.isConnected()) {
    Serial1.println("Old connection active. Closing");
    wifly.close();
  }
}

void getConnectedWithServer() {
  while(!wifly.open(server,serverPort)){
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
  }
  Serial1.println("Xmpp connection established");
  yellow.off();   
  green.on();
}

void loop(){
  if(!wifly.isConnected()) {
    green.off();
    yellow.off();
    red.on();
    // reconnect
    getConnectedWithServer();
  } else if(!xmpp.getConnected()) {
    green.off();
    yellow.on();
    // reconnect
    getConnectedWithXMPP();  
  } 
  else {
    xmpp.handleIncoming(); 
    sendData = xmpp.getRecAvailable();
  }
  
  if(sendData && millis() - currentTime > (reportStep*1000)){
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
}

void flicker(){
  blue.on();
  delay(50);
  blue.off(); 
}


