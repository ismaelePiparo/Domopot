package com.example.domopotapp.ui.main

import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainViewModel : ViewModel(){

    var wifiManager : WifiManager? = null
    var ssid : String = ""
    var ipWebServer : String = "http://192.168.4.1"
    var Pot_ID : String = ""
    var timestamp : Long = 0
    val db = Firebase.database.reference
    var mAuth = FirebaseAuth.getInstance();
    var myPots = mutableMapOf<String, String>()
    var currentPot : String = ""

    lateinit var googleSignInClient: GoogleSignInClient

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("226494663795-37g5amr4f0hffbji6c1k057td9tmv0lq.apps.googleusercontent.com")
        .requestEmail()
        .build()

}