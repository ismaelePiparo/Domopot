#define MEASUREMENTS 1

int soil_pin = A0; // AOUT pin on sensor
int m_count = 0;
float measurements[MEASUREMENTS];
float sum = 0;

void setup() {
  Serial.begin(9600); // serial port setup
  analogReference(EXTERNAL); // set the analog reference to 3.3V
}

void loop() {
  float last_measurement = analogRead(soil_pin);

  measurements[m_count % MEASUREMENTS] = last_measurement;
  
  sum += last_measurement;
  
  if (m_count >= MEASUREMENTS) {
    sum -= measurements[(m_count + 1) % MEASUREMENTS];
    Serial.print("Soil Moisture Sensor Voltage: ");
    Serial.print((double(sum / MEASUREMENTS) / 1023.0) * 3.3); // read sensor
    Serial.println(" V");
  }

  m_count++;
  delay(10);
}
