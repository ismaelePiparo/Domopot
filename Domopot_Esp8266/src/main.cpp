#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <Firebase_ESP_Client.h> //https://github.com/mobizt/Firebase-ESP-Client?utm_source=platformio&utm_medium=piohome#realtime-database
#include <EEPROM.h>
#include <Wire.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

#define BYTES_RX 6
#define DATABASE_URL "domopotdb-default-rtdb.firebaseio.com"
#define PROJECT_ID "domopotdb-default-rtdb"   
#define FIREBASE_AUTH "nW6ATSSZ2bL3WBD5DaZH5XS3mpNT6csbKgLjFQhw"
#define PASSWORD_OFFSET 63
#define SSID_OFFSET 32
#define AP_SSID "DomoPot_WiFi"
#define AP_PASS ""

/* Tenere attivo l'accesspoint in ascolto solo per handle start e credentials
 * 
 */



uint8_t max_connections=8;//Maximum Connection Limit for AP
int current_stations=0, new_stations=0;

const long utcOffsetInSeconds = 3600;
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP);

//FIREBASE
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;


//sensori
float humidity = 0;
int waterLvl = 0;

//Enum per comunicare con arduino
enum led_state{
  waterLevel,
  accessPoint,
  connected,
  off
};

led_state ledState;

//Specifying the Webserver instance to connect with HTTP Port: 80
ESP8266WebServer server(80);


//Inizializzazione delle variabili. ssid e passwordo sono quelli che verranno passati via app
bool onLine = false;
String Pot_ID = "DomoPot_01";
String ssid = "UNKNOWN";
String pass = "UNKNOWN";

//comunicazione con arduino
int requestData(void);
void SetArduinoState(led_state state);
//configurazione
void ConfigurationPhase();
void APWhileConnected();
void connectToWifi (String ssid, String pass);
void AccessPoint(String ap_ssid, String ap_password);
void handle_Credentials();
void handle_Start();
void handle_NotFound();
void SetupWait(int secs);
//EEPROM
void writeStringToEEPROM(int addrOffset, const String &strToWrite);
String readStringFromEEPROM(int addrOffset);
void SaveWiFiCreds();
void RestoreWiFiCreds();
//firebase
void FirebaseSetup();
void FirebasePrintTime();


void setup() {
  Serial.begin(9600);
  EEPROM.begin(512);
  FirebaseSetup();
  Wire.begin();
  //WiFi.mode(WIFI_AP_STA);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN,HIGH);

  //RestoreWiFiCreds();           //recupera credenziali dalla EEPROM se ci sono
  connectToWifi(ssid,pass);     //tentativo di connessione
  Serial.println("Attivazione AP");
  AccessPoint(AP_SSID, AP_PASS);

  if(WiFi.status() != WL_CONNECTED){
    SetArduinoState(ledState = accessPoint);
    Serial.println("Entro in ConfigurationPhase");
    ConfigurationPhase();       //non ritorna finché non si è connessi all'AP
  }
  timeClient.begin(); //connessione al server NTP(?)
  SetArduinoState(ledState = connected);
  //ESP CONNESSO
  onLine = true;
  
}

//Solo quando è onLine entra nel loop
void loop() {
  //APWhileConnected();
  if(WiFi.status() != WL_CONNECTED){ // TO DO: implementare una procedura robusta per quando cade la connessione
    connectToWifi(ssid,pass);  
  }else{
    //Led dell'esp acceso per vedere che è connesso ad internet in fase di debug
    digitalWrite(LED_BUILTIN,LOW);
    //fai cose online TO DO: implementare come fare le cose online
    requestData(); //richede e stampa i dati di arduino
    delay(2000);
    FirebasePrintTime();
  }
  //Da gestire il fatto che potrebbe mancare la rete wifi e bisogna riconnettersi
}

#pragma region TempHideCode

//ricevi dati da Arduino
int requestData(){
  Serial.println("requesting data...");
  Wire.requestFrom(1,6);
  
  byte data[BYTES_RX];
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

//di ad Arduino come comportarsi
void SetArduinoState(led_state state){
  Wire.beginTransmission(1); //indizzo Arduino
  Wire.write(lowByte(state));        
  Wire.endTransmission();
}
//attesa di 4 secondi
void SetupWait(int secs){
    for (uint8_t t = secs; t > 0; t--) {
    Serial.printf("[SETUP] WAIT %d...\n", t);
    Serial.flush();
    delay(1000);
  }
}

//Configurazione primo utilizzo
void ConfigurationPhase (){

  //Fin quando non è connesso a nessuna rete si comporta da web server
  while ((WiFi.status() != WL_CONNECTED))
  {
    //Serial.print(".");
    delay(100);
    server.handleClient();
    //Continuously check how many stations are connected to Soft AP and notify whenever a new station is connected or disconnected
    new_stations=WiFi.softAPgetStationNum();
   
    if(current_stations<new_stations)//Device is Connected
    {
      current_stations=new_stations;
      Serial.print("New Device Connected to SoftAP... Total Connections: ");
      Serial.println(current_stations);
    }
   
    if(current_stations>new_stations)//Device is Disconnected
    {
      current_stations=new_stations;
      Serial.print("Device disconnected from SoftAP... Total Connections: ");
      Serial.println(current_stations);
    }
  }
}

//Comportamento dell'AP mentre si ha connessione al wifi
void APWhileConnected(){
  
  server.handleClient();
  new_stations=WiFi.softAPgetStationNum();
  
}

//Funzione che prova a connettersi ad una rete
void connectToWifi (String ssid, String pass)
{
  Serial.println("Try to connect...");
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid,pass);

  while (true)
  {
    delay(100);
    if(WiFi.status() == WL_CONNECTED){
      Serial.println("Connected to " + ssid);
      //WiFi.softAPdisconnect(false); //disconnette i client dall'ap senza spegnerlo
      break;
    }
    if(WiFi.status() == WL_CONNECT_FAILED || WiFi.status() == WL_NO_SSID_AVAIL){
      Serial.println("Connection failed");
      break;
    }
  }
  

}

//Inizializza e e configura access point e webserver locale
void AccessPoint(String ap_ssid, String ap_password)
{
  WiFi.mode(WIFI_AP);
  if(WiFi.softAP(ap_ssid,ap_password,1,false,max_connections)==true)
  {
    Serial.print("Access Point is Created with SSID: ");
    Serial.println(ap_ssid);
    Serial.print("Max Connections Allowed: ");
    Serial.println(max_connections);
    Serial.print("Access Point IP: ");
    Serial.println(WiFi.softAPIP());
  }
  else
  {
    Serial.println("Unable to Create Access Point");
  }
 
  //Specifying the functions which will be executed upon corresponding GET/POST request from the client
  server.on("/", handle_Start);
  server.on("/credentials", handle_Credentials);
  server.onNotFound(handle_NotFound); 
  
  //Starting the Server
  server.begin();
  Serial.println("HTTP Server Started");
}

//legge i parametri che gli arrivano via web request dall'app (ssid e pass)
//e prova a connettersi
void handle_Credentials()
{
  String message = "Credentials Received";
  message += "\n";                            

  //salvataggio parametri ricevuti
  for (int i = 0; i < 2; i++) {
    if (server.argName(i) == "ssid")
    {
      ssid = server.arg(i);
    }else if (server.argName(i) == "pass")
    {
      pass = server.arg(i);
    }else{
      message = "ERROR";  
    }
   
  } 

  if(message=="ERROR"){
    message = "Invalid Arguments";
    Serial.println(message);
    server.send(200, "text/plain", message);
  }else{
    Serial.println(message);
    Serial.println("SSID is: " + ssid);
    Serial.println("Password is: " + pass);
    server.send(200, "text/plain", "Try to connect...");
    delay(5000);
    connectToWifi(ssid,pass);
  }
  if(WiFi.status() == WL_CONNECTED){
    //SaveWiFiCreds();
  }
}

//invia l'id su richiesta
void handle_Start()
{
  server.send(200, "text/plain", Pot_ID);
}

//implementa il 404
void handle_NotFound()
{
  server.send(404, "text/plain", "Not found");
}

void writeStringToEEPROM(int addrOffset, const String &strToWrite)
{
  byte len = strToWrite.length();
  EEPROM.write(addrOffset, len);
  for (int i = 0; i < len; i++)
  {
    EEPROM.write(addrOffset + 1 + i, strToWrite[i]);
  }
  if (EEPROM.commit()) Serial.println("EEPROM COMMITTED");
}

String readStringFromEEPROM(int addrOffset)
{
  int newStrLen = EEPROM.read(addrOffset);
  char data[newStrLen + 1];
  for (int i = 0; i < newStrLen; i++)
  {
    data[i] = EEPROM.read(addrOffset + 1 + i);
  }
  data[newStrLen] = '\0';
  return String(data);
}

void SaveWiFiCreds(){
    writeStringToEEPROM(0,ssid);
    writeStringToEEPROM(100,pass);
    Serial.println("Credentials saved");
}

void RestoreWiFiCreds(){
    ssid = readStringFromEEPROM(0);
    pass = readStringFromEEPROM(100);
    if(ssid.length() <= SSID_OFFSET+1 && pass.length() <= PASSWORD_OFFSET+1){
      Serial.println("Recovered creds:");
      Serial.println("Ssid: "+ssid);
      Serial.println("Pass: "+pass);
    } else{
      Serial.println("No credentials saved\nFound: " + ssid + "\n"+pass);
    }
    
}

#pragma endregion

void FirebaseSetup(){
  config.host = PROJECT_ID;
  config.api_key = FIREBASE_AUTH;
  Firebase.begin(&config, &auth);
  config.database_url = DATABASE_URL;

  config.signer.test_mode = true;

  /**
   Set the database rules to allow public read and write.

      {
      "rules": {
        ".read": true,
        ".write": true
      }
      }

  */

  Firebase.reconnectWiFi(true);

  /* Initialize the library with the Firebase authen and config */
  Firebase.begin(&config, &auth);

  //Or use legacy authenticate method
  //Firebase.begin(DATABASE_URL, DATABASE_SECRET);
}

void FirebasePrintTime(){
  timeClient.update();
  Firebase.RTDB.setString(&fbdo, '/'+Pot_ID+"/OnlineStatus/ConnectTime", String(timeClient.getEpochTime()));
}