#include <SensorProtocol.h>

int sensorTypes[] = {
  1, 2, 3};
int locationID = 1;
int length = sizeof(sensorTypes) / sizeof(int);
SensorProtocol protocol(locationID);

void setup() {
  Serial.begin(9600);
  protocol.setSerial(&Serial);
  protocol.setSensors(sensorTypes, length);
  protocol.addValue(1, 1.29);
  protocol.addValue(2, 6.43);
  protocol.addValue(3, 22.3);
  char* message = protocol.createMessage();
  Serial.println(message);
}

void loop() {
  
}


