#include <Arduino.h>
#include <Xmpp.h>

XMPP xmpp("arduino", "arduinomega", "sensor", "arduino-xmpp");
int counter = 0;

void setup(){
  Serial.begin(9600);
  xmpp.setSerial(&Serial);
  Serial.println("Serial started");
  delay(1000);
}

void loop(){
  if(xmpp.getConnected()){
    Serial.println("connected...");
    counter ++;
    if(counter > 5) {
      Serial.println(xmpp.closeStream());
      counter = 0;
    }
  } 
  else {
    Serial.println(xmpp.createStanza());
    xmpp.processInput("blabla");
  }
  delay(1000);
}








