#include <Arduino.h>
#include <Wire.h>

#define bytes 6

float humidity = 0;
int waterLevel = 0;

int requestData(void);

void setup() {
  Serial.begin(115200);
  Wire.begin();
}

void loop() {
  if(requestData()==0){
    Serial.print(humidity);
    Serial.println(" V");
    Serial.print(waterLevel);
    Serial.println(" cm");
  } else{
    Serial.println("Arduino didn't answer :(");
  }
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
    waterLevel = (data[5]<<8)+data[4];
    return 0;
  }else{
    return 1;
  }
}