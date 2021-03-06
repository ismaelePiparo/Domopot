#include <Arduino.h>
#include <Wire.h>
#include "FastLED.h"

/*** APPUNTI
 * Nota bene: tutte le funzioni di animazione dei led sono pensate per ritornare subito
 * e per essere chiamate ripetutamente nel loop principale.
***/

/*to do:
- Programmazione e caratterizzazione pompa dell'acqua
*/

#pragma region Macros
//pins
#define echoPin 3 // attach pin D2 Arduino to pin Echo of HC-SR04
#define trigPin 2 //attach pin D3 Arduino to pin Trig of HC-SR04
#define ledPin 6 //pin di dati dei LED
#define humPin A0 // pin sensore umidità
#define pumpPin 9 //pin della pompa dell'acqua

//tempo di attivazione della pompa
#define PUMP_TIME 2000

//Misura umidità
#define humSamples 1 //numero di campioni su cui fare la media dell'umidità
#define humTime 100 //tempo tra una misura dell'umidità e la successiva in ms

//Parametri LEDs
#define NUM_LEDS 5 //numero di led
#define depth 30  //profondità massima da misurare
#define alpha 0.3 //sensibilità del sensore (veloce -> jitter, lento -> poco responsivo)
#pragma endregion

// Variabili
int distance; // variable for the distance measurement
int count; //posizione led errore
enum Arduino_tx{
  pumpWater,
  Led_waterLevel,
  Led_accessPoint,
  Led_connected,
  Led_off
};

Arduino_tx ledState = Led_waterLevel;


CRGB leds[5];
#pragma region Prototipi funzioni
void sendData();
int MeasureDistance();
void ShowDistance();
void OutOfRangeAnimation();
void ShowDistanceAnimation();
void LedsOff();
float MeasureHumidity();
void ReceiveState(int n);
void AccessPointAnimation();
void PumpWater();
#pragma endregion

void setup() {
  pinMode(trigPin, OUTPUT); // Sets the trigPin as an OUTPUT
  pinMode(echoPin, INPUT); // Sets the echoPin as an INPUT
  pinMode(pumpPin, OUTPUT);
  analogReference(EXTERNAL); // set the analog reference to 3.3V (AREF collegato a 3.3)
  FastLED.addLeds<NEOPIXEL, ledPin>(leds, NUM_LEDS);
  Wire.begin(1);     //diventa slave all'indirizzo 1;    
  Wire.onRequest(sendData);   //imposta callback per mandare dati su richiesta
  Wire.onReceive(ReceiveState);
  Serial.begin(9600);
}

void loop() {
/* CODICE DI DEBUG!!! Loop tra stati per testare i led (codice temporaneo)
  if((millis() % 6000) < 3000){
    ledState = Led_accessPoint;
  }else{
    ledState = Led_accessPoint;
  }
*/


  // Calcola la distanza e mostrala sui led o mostra un errore
  switch(ledState){
    case Led_waterLevel:
        ShowDistance();
        break;
    case Led_accessPoint:
        AccessPointAnimation();
        break;
    case Led_connected:
        Serial.println("Connection behaviour not implemented yet");
        break;
    case Led_off:
        LedsOff();
        break;
  }
  
  Serial.print("Distance: ");
  Serial.print(distance);
  Serial.println(" cm");
  Serial.print("Humidity: ");
  Serial.print(MeasureHumidity());
  Serial.println(" V");
  
}

void sendData() {
  float humidity = MeasureHumidity();
  int level = MeasureDistance();  
  Wire.write((const byte*)(&humidity),4);
  Wire.write((const byte*)(&level),2 );
  Serial.println("Data was sent");
}

#pragma region distanza
int MeasureDistance() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  // Sets the trigPin HIGH (ACTIVE) for 10 microseconds
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  // Reads the echoPin, returns the sound wave travel time in microseconds
  long distance = pulseIn(echoPin, HIGH);
  return distance * 0.034 / 2;
}

void OutOfRangeAnimation(){
  count++;  //avanza animazione di out-of-range
  count = millis() / 300;
  
  int phase = count % (NUM_LEDS * 2 - 2);
  for (int i = 0; i < NUM_LEDS; i++) {
    if (phase >= NUM_LEDS) {
      phase = (NUM_LEDS * 2 - 2) - phase ;
    }
    if (i == phase) {
     leds[i] = CRGB::Red;
    } else {
      leds[i] = CRGB::Black;
    }
  }
  FastLED.show();
}

void ShowDistance(){
  distance =  (1 - alpha) * distance + alpha * MeasureDistance(); // filtro passa basso sulla misura della distanza

  if (distance > depth + (depth / NUM_LEDS)) { //La distanza rientra nel range di interesse?
    OutOfRangeAnimation(); //No
  } else {
    ShowDistanceAnimation(); //Si
  }
}

void ShowDistanceAnimation(){
    int interval = distance / NUM_LEDS;
    int subInterval = distance % NUM_LEDS;
    int waterHue = 130;

    for (int i = 0; i < NUM_LEDS; i++) {
      if (i > interval) {   // led acceso
        leds[i] = CHSV(waterHue, 255, 255);
      }
      else if (i < interval) {    //led spento
        leds[i] = CRGB::Black;
      }
      else {  //led dimmato
        leds[i] = CHSV(waterHue, 255, ((255 - subInterval) * 255) / NUM_LEDS);
      }
    }
    FastLED.show();
}
#pragma endregion

void LedsOff(){
  FastLED.clear(true);
}

void AccessPointAnimation(){
  int orangeHue = 32;
  int value = (1 + sin(0.3*float(millis())/1024*6.28)) * 90 + 70;
  for (int i = 0; i < NUM_LEDS; i++) {
    leds[i] = CHSV(orangeHue, 255, value);
  }
  FastLED.show();
}

float MeasureHumidity(){ //per ora ritorna la tensione misurata dal sensore
  float samples = 0;
  for(int i = 0; i < humSamples; i++){
    samples += analogRead(humPin);
    delay(humTime);
  }

  samples /= humSamples; //media dei campioni
  samples *= (3.3/1023); // conversione da int a Volt

  return samples;
}

void PumpWater(){
    digitalWrite(pumpPin, HIGH);
    delay(PUMP_TIME);
    digitalWrite(pumpPin, LOW);
}

void ReceiveState(int n){
  if(n == 1){
    Arduino_tx messageReceived = (Arduino_tx)Wire.read();
    if(messageReceived == pumpWater){
      PumpWater();
    } else{
      ledState = (Arduino_tx)Wire.read();
      Serial.println("Nuovo stato: " + ledState);
    }
    
  } else{
    Serial.println("trasmissione non valida");
  }
}