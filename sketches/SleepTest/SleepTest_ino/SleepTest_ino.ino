
#include <Ports.h>
#include <WiFly.h>
#include <SPI.h>

ISR(WDT_vect) { Sleepy::watchdogEvent(); }

void setup() {
  Serial.begin(9600);
  WiFly.setUart(&Serial);
  WiFly.begin();
  WiFly.configure(WAKE_TIMER, 15);
  WiFly.configure(SLEEP_TIMER, 5);
  WiFly.configure(WLAN_JOIN, 1);
  while(!WiFly.join("poku", "pokunett")){
    Serial.println("Join fail");
    delay(1000);
  }
}

void loop() {
  // put your main code here, to run repeatedly: 
  Sleepy::loseSomeTime(30000);
}
