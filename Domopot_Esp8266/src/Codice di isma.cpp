#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ESP8266Firebase.h>
#include <EEPROM.h>
#include <Wire.h>

#define BYTES_RX 6
#define PROJECT_ID "domopotdb-default-rtdb"   
#define PASSWORD_OFFSET 63
#define SSID_OFFSET 32
#define AP_SSID "DomoPot_WiFi"
#define AP_PASS ""

uint8_t max_connections=8;//Maximum Connection Limit for AP
int current_stations=0, new_stations=0;

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

//Specifying the Webserver instance to connect with HTTP Port: 80
ESP8266WebServer server(80);

Firebase firebase(PROJECT_ID);

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
void connectToWifi (String ssid, String pass);
void AccessPoint(String ap_ssid, String ap_password);
void handle_Credentials();
void handle_Start();
void handle_NotFound();
void SetupWait();
//EEPROM
void writeStringToEEPROM(int addrOffset, const String &strToWrite);
String readStringFromEEPROM(int addrOffset);
void SaveWiFiCreds();
void RestoreWiFiCreds();

void setup() {
  Serial.begin(9600);
  EEPROM.begin(512);
  Wire.begin();

  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN,HIGH);

  SetupWait();

  RestoreWiFiCreds();           //recupera credenziali dalla EEPROM se ci sono
  connectToWifi(ssid,pass);     //tentativo di connessione
 
  if(WiFi.status() != WL_CONNECTED){
    ConfigurationPhase();       //non ritorna finché non si è connessi all'AP
  }

  //ESP CONNESSO
  Serial.println("Connesso alla rete: " + ssid);
  onLine = true;
  //Una volta connesso l'esp si collega al DB dicendo che è onLine...
  SaveWiFiCreds(); //Salva credenziali nella EEPROM
  firebase.setInt(Pot_ID+"/onLine", 1);
}

//Solo quando è onLine entra nel loop
void loop() {
  if(WiFi.status() != WL_CONNECTED){
    connectToWifi(ssid,pass);  //TODO: come tornare alla modalità AP?
  }else{
    //fai cose online
    requestData(); //richede e stampa i dati di arduino
    delay(2000);
  }
  //Da gestire il fatto che potrebbe mancare la rete wifi e bisogna riconnettersi
}

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
void SetupWait(){
    for (uint8_t t = 4; t > 0; t--) {
    Serial.printf("[SETUP] WAIT %d...\n", t);
    Serial.flush();
    delay(1000);
  }
}

//Configurazione primo utilizzo
void ConfigurationPhase (){
  
  Serial.println("non connesso...attivazione AP");
  AccessPoint(AP_SSID, AP_PASS);

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

//Funzione che prova a connettersi ad una rete
void connectToWifi (String ssid, String pass)
{
  Serial.println("Try to connect...");
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid,pass);
}

//Inizializza e e configura access point e webserver locale
void AccessPoint(String ap_ssid, String ap_password)
{
  Serial.println("Starting in AP mode...");
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
    writeStringToEEPROM(5,ssid);
    writeStringToEEPROM(SSID_OFFSET+10,pass);
}

void RestoreWiFiCreds(){
    ssid = readStringFromEEPROM(5);
    pass = readStringFromEEPROM(SSID_OFFSET+10);
    if(ssid.length() <= SSID_OFFSET+1 && pass.length() <= PASSWORD_OFFSET+1){
      Serial.println("Recovered creds:");
      Serial.println("Ssid: "+ssid);
      Serial.println("Pass: "+pass);
    } else{
      Serial.println("No credentials saved");
    }
    
}


