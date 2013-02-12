#include <Xmpp.h>
#include <WiFly.h>
#include <SensorProtocol.h>

char* recipient = "arduinoserver@arduino-xmpp";
XMPP xmpp("arduino","arduinomega","sensor","arduino-xmpp", recipient);
//char* serverAddress = "192.168.1.73";
char* serverAddress = "172.19.5.17";
int serverPort = 5222;
WiFlyClient client(serverAddress, serverPort);

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

  while(!WiFly.join()){
    Serial.println("Join fail");
    delay(1000);
  }
  if(client.connect()){
    // let properly initialize the connection
    delay(2000);
    Serial1.println("connected");
    xmpp.setClient(&client);
    if(xmpp.connect()){
      Serial1.println("Xmpp connection established");
      protocol.setSensors(sensorTypes, length);
      //yellow.off();   
      //green.on();
    }
  } 
  else {
    Serial1.println("Connecting to server failed");
    // Hang on failure. 
  }
}

void loop(){
  if(!xmpp.getConnected()) {
    //green.off();
    //yellow.on();
    // not sending data
    while(true);
    // start reconnection again in some time
  } 
  else {
    xmpp.handleIncoming(); 
    sendData = xmpp.getRecAvailable();
  }
  if(sendData && millis() - currentTime > (reportStep*1000)){
    //float tm = thermistor.getCelsius();
//    float hs = hall.get();
//    float ldr = light.get();
  //  flicker();
    currentTime = millis(); 
    protocol.addValue(1, 1.1);
    protocol.addValue(2, 11.1);
    protocol.addValue(3, 111);
    char* message = protocol.createMessage();
    xmpp.sendMessage(recipient, message, "chat");
  }
}


