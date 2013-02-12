#include <Base64.h>
#include <XMPPClient.h>
#include <Ethernet.h>
#include <SPI.h>
#include <avr/pgmspace.h>

// Update these with values suitable for your network.
byte mac[]    = {  0x90, 0xA2, 0XDA, 0X00, 0XF8, 0X06 };
IPAddress server(192,168,1,81);
XMPPClient client;

void setup()
{
  Serial.begin(9600);
  client.setUart(&Serial);
  Ethernet.begin(mac);
  Serial.println("Ethernet connection established");
  /* Connect to the XMPP server */
  if (client.connect(server, 5222, "test1", "amdx2", "arduinos", "test1")) {
    Serial.println("XMPP server connection established");
    /* Connected, let everyone know that we're online */
    client.sendPresence();
  } else {
    Serial.println("Connection failed");
    while(1);    
  }
}

int count = 0;
void loop() {
  Serial.println("Sending hello");
  /* Say hello! */
  client.sendMessage("test2@amdx2", "hello");
  delay(5000);
  
  /* A couple of times... */
  if(count++ > 0) {
    /* Say goodbye! */
    client.sendMessage("test2@amdx2", "bye!");
    /* Close the connection */
    client.close();
    /* Spin forever */
    while(1) {;}
  }
}




