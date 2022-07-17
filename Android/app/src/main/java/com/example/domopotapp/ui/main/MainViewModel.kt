package com.example.domopotapp.ui.main

import android.net.wifi.WifiManager
import android.provider.Settings.Global.getString
import androidx.lifecycle.ViewModel
import com.example.domopotapp.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainViewModel : ViewModel(){

    public var wifiManager : WifiManager? = null
    public var ssid : String = ""
    public var ipWebServer : String = "http://192.168.4.1"
    public var Pot_ID : String = ""
    public var timestamp : Long = 0
    public val db = Firebase.database.reference
    public var mAuth = FirebaseAuth.getInstance();
    public var myPots = mutableMapOf<String, String>()
    public var currentPot : String = ""
    public lateinit var googleSignInClient: GoogleSignInClient

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("226494663795-37g5amr4f0hffbji6c1k057td9tmv0lq.apps.googleusercontent.com")
        .requestEmail()
        .build()



}