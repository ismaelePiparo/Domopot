/*#include <Arduino.h>
#include <Wire.h>
#include <ESP8266Firebase.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>


#define bytes 6
#define PROJECT_ID "domopotdb-default-rtdb"  

const char* ap_ssid = "DomoPot_WiFi"; //Access Point SSID
const char* ap_password= ""; //Access Point Password
uint8_t max_connections=8;//Maximum Connection Limit for AP
int current_stations=0, new_stations=0;

ESP8266WebServer server(80);
Firebase firebase(PROJECT_ID);

//Inizializzazione delle variabili. ssid e passwordo sono quelli che verranno passati via app
bool onLine = false;
String Pot_ID = "DomoPot_01";
String ssid = "UNKNOWN";
String pass = "UNKNOWN";

float humidity = 0;
int waterLvl = 0;

enum led_state{
  waterLevel,
  accessPoint,
  connected,
  off
};

int requestData(void);

void setup() {
  Serial.begin(115200);
  Wire.begin();

  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN,HIGH);

  for (uint8_t t = 4; t > 0; t--) {
    Serial.printf("[SETUP] WAIT %d...\n", t);
    Serial.flush();
    delay(1000);
  }
  
  

}

void loop() {
  requestData();
  delay(2000);
}

int requestData(){
  Serial.println("requesting data...");
  Wire.requestFrom(1,6);
  
  byte data[bytes];
  int i = 0;
  
  while(Wire.available()){
    //Serial.println("Received byte");
    data[i] = Wire.read();
    i++;
  }

  if(i==6){
    memcpy(&humidity, &data, sizeof(humidity));
    waterLvl = (data[5]<<8)+data[4];
    Serial.print(humidity);
    Serial.println(" V");
    Serial.print(waterLvl);
    Serial.println(" cm");
    return 0;
  }else{
    Serial.println("No correct answer from arduino :(");
    return 1;
  }
}*/