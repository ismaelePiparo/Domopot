#include <WString.h>

enum Arduino_tx{
  Led_waterLevel,
  Led_accessPoint,
  Led_connected,
  Led_off
};

//comunicazione con arduino
int requestData(void);
void SendMessageToArduino(Arduino_tx state);
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
//FIREBASE
void FirebaseSetup();