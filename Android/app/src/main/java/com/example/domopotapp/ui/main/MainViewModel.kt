package com.example.domopotapp.ui.main

import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainViewModel : ViewModel(){

    public var wifiManager : WifiManager? = null
    public var ssid : String = ""
    public var ipWebServer : String = "http://192.168.4.1"
    public var Pot_ID : String? = null
    public val db = Firebase.database.reference



}