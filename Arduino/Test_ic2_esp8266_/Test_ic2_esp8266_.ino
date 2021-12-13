#include <Wire.h>
#include <ESP8266Firebase.h>
#include <ESP8266WiFi.h>

#define _SSID "Yolo"        // Your WiFi SSID
#define _PASSWORD "bvpt1442"    // Your WiFi Password
#define PROJECT_ID "lab03-cd1f2-default-rtdb"   // Your Firebase Project ID. Can be found in project settings.



Firebase firebase(PROJECT_ID);    // SLOW BUT HASTLE-FREE METHOD FOR LONG TERM USAGE. DOES NOT REQUIRE PERIODIC UPDATE OF FINGERPRINT

void setup() {
  //Sign of life
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);

  //SERIAL
  Serial.begin(115200);
  
  //IC2
  Wire.begin();     //Configura il dispositivo come Master

  //WI-FI
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  delay(1000);
  
  // Connect to WiFi
  Serial.println();
  Serial.println();
  Serial.print("Connecting to: ");
  Serial.println(_SSID);
  WiFi.begin(_SSID, _PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print("-");
  }

  Serial.println("");
  Serial.println("WiFi Connected");

  // Print the IP address
  Serial.print("Use this URL to connect: ");
  Serial.print("http://");
  Serial.print(WiFi.localIP());
  Serial.println("/");
  digitalWrite(LED_BUILTIN, HIGH);

  //FIREBASE
}

void loop() {
  Serial.println("Requesting data....");
  Wire.requestFrom(1, 4); // richiede 4 byte da dispositivo #1
  int i = 0;
  byte data[4];     // buffer in cui leggere i dati
  while (Wire.available()) {
    data[i] = Wire.read();
    i++;
  }
  if (i == 4) { //when I got 4 bytes
    int v1 = (data[1] << 8) + data[0]; //reassemble int from the two bytes
    int v2 = (data[3] << 8) + data[2];
    Serial.print("humidity:");
    Serial.println(v1);
    Serial.print("level:");
    Serial.println(v2);

    firebase.setInt("Siamo fighi/quanto siamo umidi", v1);
    firebase.setInt("Siamo fighi/quanto acqua conteniamo", v2);   
  } else {
    Serial.println("Error");
  }
  delay(1000);
}
