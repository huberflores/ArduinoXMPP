#include "WiFly.h"

char* ssid = "ThomsonF218A5";
char* pass = "DAE2073CA5";

char* serverAddress = "192.168.1.77";
int serverPort = 5222;

WiFlyClient client(serverAddress, serverPort);

void setup(){  
  Serial.begin(9600);
  Serial.println("Setup");
  WiFly.setUart(&Serial);
  WiFly.begin();

  if (!WiFly.join()) {
    Serial.println("Join fail");  
    while (1) {
      // Hang on failure.
    }
  }
  if(client.connect()){
      
  } else {
     // Hang on failure. 
  }
}

void loop(){
  
}
