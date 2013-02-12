#include <SPI.h>
#include <Ethernet.h>
#include <SensorProtocol.h>
#include <TinkerKit.h>

byte mac[] = { 
  0x90, 0xA2, 0XDA, 0X00, 0XF8, 0X06 };
EthernetServer server(2000);

int sensorTypes[] = { 
  1, 2, 3 };
int locationID = 1;
int length = sizeof(sensorTypes) / sizeof(int);
SensorProtocol protocol(locationID);

TKThermistor thermistor(I0);
TKHallSensor hall(I1);
TKLightSensor light(I2);

void setup() {
  Serial.begin(9600);
  Serial.println("Serial started");
  Ethernet.begin(mac);
  Serial.println("Ethernet started");
  server.begin();
  Serial.println("Server stared");
  protocol.setSensors(sensorTypes, length);
}

void loop() {
  EthernetClient client = server.available();
  if(client) {
    Serial.println("Client connected");
    boolean currentLineIsBlank = true;
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        // if you've gotten to the end of the line (received a newline
        // character) and the line is blank, the http request has ended,
        // so you can send a reply
        if (c == '\n' && currentLineIsBlank) {
          // send a standard http response header
          client.println("HTTP/1.1 200 OK");
          client.println("Content-Type: text/html");
          client.println();

          float tm = thermistor.getCelsius();
          float hs = hall.get();
          float ldr = light.get();
          protocol.addValue(1, tm);
          protocol.addValue(2, hs);
          protocol.addValue(3, ldr);
          char* message = protocol.createMessage();
          Serial.print("Sending message: ");
          Serial.println(message);
          client.println(message);
          break;
        }
        if (c == '\n') {
          // you're starting a new line
          currentLineIsBlank = true;
        } 
        else if (c != '\r') {
          // you've gotten a character on the current line
          currentLineIsBlank = false;
        }
      }
    }
    // give the web browser time to receive the data
    delay(1);
    client.flush();
    client.stop();
  }
}







