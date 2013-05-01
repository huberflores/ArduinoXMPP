#include <Ports.h>
#include <WiFlyHQ.h>
#include <SensorProtocol.h>
#include <TinkerKit.h>
#include <aJSON.h>
#include <MemoryFree.h>

ISR(WDT_vect) { Sleepy::watchdogEvent(); }

char* server = "192.168.43.231";
//char* server = "ec2-54-224-196-78.compute-1.amazonaws.com";
int serverPort = 8080;

int avgWakeUpTime = 5;

WiFly wifly;

/* Change these to match your WiFi network */
const char mySSID[] = "poku";
const char myPassword[] = "pokunett";

int sensorTypes[] = { 
  1, 2, 3 };
int locationID = 1;
int length = sizeof(sensorTypes) / sizeof(int);
SensorProtocol protocol(locationID);

TKLed red(O0);
TKLed yellow(O4);
TKLed green(O2);
TKLed blue(O3);
TKThermistor thermistor(I0);
TKHallSensor hall(I1);
TKLightSensor light(I2);

void setup(){
  red.on();
  Serial.begin(9600);
  Serial1.begin(9600);
  Serial1.println("Serial1 started");
  protocol.setSensors(sensorTypes, length);
  getConnected();
  getConnectedWithServer();
}

void getConnected() {
  while(!wifly.begin(&Serial, &Serial1)) {
    Serial1.println("Failed to start wifly");
    delay(1000);
  }
  
  wifly.setJoin(1);
  wifly.setWakeTimer(0);
  wifly.setFlushSize(1420);
  wifly.setTxPower(10);
  

  /* Join wifi network if not already associated */
  if (!wifly.isAssociated()) {
    /* Setup the WiFly to connect to a wifi network */
    Serial1.println("Joining network");
    wifly.setSSID(mySSID);
    wifly.setPassphrase(myPassword);
    wifly.enableDHCP();
    
    while(!wifly.join()) {
      Serial1.println("Join failed");
    } 
    Serial1.println("Joined wifi network");
  } 
  else {
    Serial1.println("Already joined network");
  }
  Serial1.println("WiFly ready");

  red.off();
  green.off();
  yellow.on();

  wifly.setDeviceID("Wifly-TCP");
  wifly.setIpProtocol(WIFLY_PROTOCOL_TCP);

  if (wifly.isConnected()) {
    Serial1.println("Old connection active. Closing");
    wifly.close();
  }
  wifly.save();
}

void getConnectedWithServer() {
  while(!wifly.open(server,serverPort, true)){
    Serial1.println("TCP Connection failed");
    wifly.close(); // Weirdly connection remains activte sometimes but isConnected returns false
    delay(1000);
  }
  green.on();
  yellow.off();
  red.off();
  Serial1.println("connected");
}

void loop(){
  Serial1.print("Loop start at ");
  Serial1.println(millis());
  
  if(!wifly.isConnected()) {
    green.off();
    yellow.off();
    red.on();
    
    // reconnect
    getConnectedWithServer();
  }
  Serial1.print("Connected at ");
  Serial1.println(millis());
  
  blue.on();
  postData();
  int idle = readResponse();
  blue.off();
  
  Serial1.print("Data sent at ");
  Serial1.println(millis());
  
  Serial1.print(">>> Available memory= ");
  Serial1.println(freeMemory());
  
  if (idle > 65) { // Max is 65 seconds for now..
    idle = 65;
  }
  
  idle = idle - avgWakeUpTime;  
  if (idle > 0) {
    wifly.close();
    delay(100);
    wifly.sleep();
    delay(100);
    Sleepy::loseSomeTime(idle * 1000);
  }
}

void postData() {
  float tm = thermistor.getCelsius();
  float hs = hall.get();
  float ldr = light.get();
  //float tm = 1.0f;
  //float hs = 1.0f;
  //float ldr = 1.0f;
  
  protocol.addValue(1, tm);
  protocol.addValue(2, hs);
  protocol.addValue(3, ldr);
  
  Message msg = protocol.createMessage();
  wifly.println("POST /data/ HTTP/1.1");
  wifly.print("Host: ");
  wifly.println(server);
  wifly.println("Connection: close");
  wifly.println("Content-Type: application/json");
  wifly.print("Content-Length: ");
  wifly.println(msg.length);
  wifly.println();
  wifly.println(msg.message);
}

int readResponse() {
  Serial1.println("readResponse");
  long startMillis = millis();
  boolean message = false;
  String line = "";
  long contentLength = 0;
  long read = 0;
  while (!(message && read >= contentLength) && (millis() - startMillis) < 5000) {
    while (wifly.available() > 0) {
      char ch = wifly.read();      
      line += ch;
      startMillis = millis(); 
      if (message) {
        read += 1;  
      } else if (line.endsWith("\r\n")) {
        if (line == "\r\n") {
          message = true;
        } else if (line.length() > 16 && line.startsWith("Content-Length: ")) {
          contentLength = line.substring(16, line.length() - 2).toInt();
        }
        line = "";
      }
    }
  }
  
  int idleTime = 0;
  if (line != "") {
    Serial1.print("Json: ");
    Serial1.println(line);
    char buf[line.length() + 1];
    line.toCharArray(buf, sizeof(buf));
    aJsonObject *root = aJson.parse(buf);
    aJsonObject *idle = aJson.getObjectItem(root, "idle");
    if (idle->type == aJson_Int) {
      idleTime = idle->valueint;
    }
    aJson.deleteItem(root);
  } else {
    Serial1.println("Invalid message received");
  }
  return idleTime; 
}

void flicker(TKLed *led) {
  led->on();
  delay(50);
  led->off(); 
}

