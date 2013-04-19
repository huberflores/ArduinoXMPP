#include <Ports.h>
#include <WiFlyHQ.h>
#include <SensorProtocol.h>
#include <TinkerKit.h>
#include <aJSON.h>
#include <MemoryFree.h>

ISR(WDT_vect) { Sleepy::watchdogEvent(); }

char* server = "192.168.43.231";
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
  wifly.println("POST / HTTP/1.1");
  wifly.println("Host: 192.168.43.231");
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
  boolean messageRead = false;
  String msg = "";
  boolean json = false;
  String line = "";
  while (!messageRead && (millis() - startMillis) < 5000) {
    while (wifly.available() > 0) {  
      char ch = wifly.read();
      startMillis = millis();
      Serial1.write(ch);
      if (ch == '{') {
        json = true;
        msg += ch;
      } else if (ch == '}') {
        messageRead = true;
        msg += ch;
      } else if (json) {
        msg += ch;
      }
    }
  }
  
  int idleTime = 0;
  if (json) {
    Serial1.println("Json found");
    char buf[msg.length() + 1];
    msg.toCharArray(buf, sizeof(buf));
    aJsonObject *root = aJson.parse(buf);
    aJsonObject *idle = aJson.getObjectItem(root, "idle");
    if (idle->type == aJson_Int) {
      Serial1.print("Idle time= ");
      Serial1.println(idle->valueint); 
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

