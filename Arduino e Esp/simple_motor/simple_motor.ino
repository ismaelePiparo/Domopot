/*
Motor speed control
*/

// Global variables we will use
int base = 6;
int speed = 0;

// Configuration of the board
void setup() {   
  // Set the pin that we will connect to the transistor base as an output
  pinMode(base,OUTPUT);
  Serial.begin(9600);
}

// Main loop
void loop() {
  // Increment the speed of the motor in three   
  // steps, each for 3 seconds

//  for (speed=150; speed<=250; speed=speed+50){
//   analogWrite(base, speed);
//   Serial.println("ok");
//   delay(3000);
//  }
  digitalWrite(base,HIGH);
  delay(1000);
  // Stop the motor for 1 second and begin again
  digitalWrite(base,LOW);
  delay(1000);
}
