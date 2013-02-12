#include <Xmpp.h>
#include <SPI.h>
#include <Ethernet.h>
#include <SensorProtocol.h>

XMPP xmpp("arduino","arduinomega","sensor","arduino-xmpp");
byte mac[] = { 
  0x90, 0xA2, 0XDA, 0X00, 0XF8, 0X06 };
IPAddress server(192,168,1,73);
EthernetClient client;
char* recipient = "arduinoserver";
uint8_t sensors[] = {1, 2, 3};

void setup(){
  // start the serial library:
  Serial.begin(9600);
  xmpp.setSerial(&Serial);
  Serial.println("Serial started...");

  // start the Ethernet connection:
  Ethernet.begin(mac);
  // give the Ethernet shield a second to initialize:
  delay(1000);
  Serial.println("connecting...");

  // if you get a connection, report back via serial
  if (client.connect(server, 5222)) {
    Serial.println("connected");
    delay(2000);
    xmpp.setClient(&client);
    if(xmpp.connect()){
      Serial.println("Xmpp connection established");      
      // set green on
    }
  } 
  else {
    // if you didn't get a connection to the server:
    Serial.println("connection failed");
  }
}

void loop(){
  if(!xmpp.getConnected()) {
    // not sending data
  }


}







