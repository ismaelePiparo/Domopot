#define echoPin 3 // attach pin D2 Arduino to pin Echo of HC-SR04
#define trigPin 2 //attach pin D3 Arduino to pin Trig of HC-SR04
#define NUM_LEDS 5 //numero di led
#define depth 30  //profondità massima da misurare
#define tono 180 //tono del colore dell'indicatore (hue in HSV)
#define alpha 0.08 //sensibilità del sensore (veloce -> jitter, lento -> poco responsivo)
#include "FastLED.h"


// defines variables
int distance; // variable for the distance measurement
int count; //posizione led errore

CRGB leds[5];

void setup() {
  pinMode(trigPin, OUTPUT); // Sets the trigPin as an OUTPUT
  pinMode(echoPin, INPUT); // Sets the echoPin as an INPUT
  Serial.begin(9600);

  FastLED.addLeds<NEOPIXEL, 6>(leds, NUM_LEDS);
}
void loop() {

  /// Calculating the distance
  distance =  (1 - alpha) * distance + alpha * MeasureDistance(); // filtro passa basso sulla misura della distanza

  if (distance > depth + (depth / NUM_LEDS)) { //La distanza rientra nel range di interesse?
    OutOfRangeAnimation(); //No
  } else {
    ShowDistance(); //Si
  }

  // Invia la distanza filtrata al monitor seriale
  Serial.print("Distance: ");
  Serial.print(distance);
  Serial.println(" cm");
}

long MeasureDistance() {
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
    delay(300);
}

void ShowDistance(){
    int interval = distance / NUM_LEDS;
    int subInterval = distance % NUM_LEDS;

    for (int i = 0; i < NUM_LEDS; i++) {
      if (i > interval) {   // led acceso
        leds[i] = CHSV(tono, 255, 255);
      }
      else if (i < interval) {    //led spento
        leds[i] = CRGB::Black;
      }
      else {  //led dimmato
        leds[i] = CHSV(tono, 255, ((255 - subInterval) * 255) / NUM_LEDS);
      }
    }
    FastLED.show();
}
