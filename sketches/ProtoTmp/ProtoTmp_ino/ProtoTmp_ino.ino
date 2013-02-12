#include <Xmpp.h>
#include <SensorProtocol.h>

//char* message = "SensorData{\"location\":1, \"data\":[{\"type\":1, \"value\":  6.07},{\"type\":2, \"value\":512.00},{\"type\":3, \"value\":295.00}]}";

char* recipient = "arduinoserver";
XMPP xmpp("arduino", "arduinomega", "sensor", "arduino-xmpp", recipient);

int sensorTypes[] = { 
  1, 2, 3 };
int locationID = 1;
int length = sizeof(sensorTypes) / sizeof(int);
SensorProtocol protocol(locationID);

void setup(){
  Serial.begin(9600);
  xmpp.setSerial(&Serial);
  xmpp.setClient(&Serial);
  protocol.setSerial(&Serial);
  protocol.setSensors(sensorTypes, length);
  protocol.addValue(1, 6.07);
  protocol.addValue(2, 512.00);
  protocol.addValue(3, 295.00);
  char* message = protocol.createMessage();
  xmpp.sendMessage(recipient, message, "chat");
}

void loop() {

}


