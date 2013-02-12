/*
 * WiFlyHQ Example httpserver.ino
 *
 * This sketch implements a simple Web server that waits for requests
 * and servers up a small form asking for a username, then when the
 * client posts that form the server sends a greeting page with the
 * user's name and an analog reading.
 *
 * This sketch is released to the public domain.
 *
 */

/* Notes:
 * Uses chunked message bodies to work around a problem where
 * the WiFly will not handle the close() of a client initiated
 * TCP connection. It fails to send the FIN to the client.
 * (WiFly RN-XV Firmware version 2.32).
 */

/* Work around a bug with PROGMEM and PSTR where the compiler always
 * generates warnings.
 */

#include <WiFlyHQ.h>
#include <SensorProtocol.h>

WiFly wifly;

/* Change these to match your WiFi network */
const char mySSID[] = "ThomsonF218A5";
const char myPassword[] = "DAE2073CA5";

int sensorTypes[] = { 
  1, 2, 3 };
int locationID = 1;
int length = sizeof(sensorTypes) / sizeof(int);
SensorProtocol protocol(locationID);

char buf[80];

void setup()
{
  Serial.begin(9600);
  Serial.println(F("Starting"));
  Serial.print(F("Free memory: "));
  Serial.println(wifly.getFreeMemory(),DEC);

  Serial1.begin(9600);
  Serial1.println(F("Serial1 Started"));

  if (!wifly.begin(&Serial, &Serial1)) {
    Serial1.println(F("Failed to start wifly"));
    wifly.terminal();
  }

  /* Join wifi network if not already associated */
  if (!wifly.isAssociated()) {
    /* Setup the WiFly to connect to a wifi network */
    Serial1.println(F("Joining network"));
    wifly.setSSID(mySSID);
    wifly.setPassphrase(myPassword);
    wifly.enableDHCP();
    wifly.save();

    if (wifly.join()) {
      Serial1.println(F("Joined wifi network"));
    } 
    else {
      Serial1.println(F("Failed to join wifi network"));
      wifly.terminal();
    }
  } 
  else {
    Serial1.println(F("Already joined network"));
  }

  wifly.setBroadcastInterval(0);	// Turn off UPD broadcast

  Serial1.print(F("MAC: "));
  Serial1.println(wifly.getMAC(buf, sizeof(buf)));
  Serial1.print(F("IP: "));
  Serial1.println(wifly.getIP(buf, sizeof(buf)));

  wifly.setDeviceID("Wifly-WebServer");

  if (wifly.isConnected()) {
    Serial1.println(F("Old connection active. Closing"));
    wifly.close();
  }

  wifly.setProtocol(WIFLY_PROTOCOL_TCP);
  if (wifly.getPort() != 2000) {
    wifly.setPort(2000);
    /* local port does not take effect until the WiFly has rebooted (2.32) */
    wifly.save();
    Serial1.println(F("Set port to 2000, rebooting to make it work"));
    wifly.reboot();
    delay(3000);
  }
  Serial1.println(F("Ready"));
  protocol.setSensors(sensorTypes, length);
}

void loop()
{
  if (wifly.available() > 0) {
    /* See if there is a request */
    if (wifly.gets(buf, sizeof(buf))) {
      if (strncmp_P(buf, PSTR("GET / "), 6) == 0) {
        /* GET request */
        Serial1.println(F("Got GET request"));
        while (wifly.gets(buf, sizeof(buf)) > 0) {
          Serial1.print("******");
          Serial1.println(buf);
        }
        Serial1.println("Req complete");
        Serial1.println(buf);
        protocol.addValue(1, 10.1);
        protocol.addValue(2, 512.0);
        protocol.addValue(3, 819.0);
        char* message = protocol.createMessage();
        Serial1.print("Sending message: ");
        Serial1.println(message);
        wifly.println(message);
      }
    }
    delay(1);
    wifly.close();
  }
}





