#include <Arduino.h>
#include <Wire.h>

#define bytes 6

float humidity = 0;
int waterLevel = 0;

int requestData(void);

void setup() {
  Serial.begin(9600);
  Wire.begin();
}

void loop() {
  
}

int requestData(){
  Wire.requestFrom(1,6);
  
  byte data[bytes];
  int i = 0;
  
  while(Wire.available()){
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