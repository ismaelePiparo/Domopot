#include <WString.h>

enum led_state{
  waterLevel,
  accessPoint,
  connected,
  off
};

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
//FIREBASE
void FirebaseSetup();