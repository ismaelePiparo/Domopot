#include <Arduino.h>
#include <Wire.h>

#define bytes 6

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
}