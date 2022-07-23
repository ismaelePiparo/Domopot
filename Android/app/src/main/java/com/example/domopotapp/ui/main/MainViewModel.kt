package com.example.domopotapp.ui.main

import android.net.wifi.WifiManager
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.example.domopotapp.*
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainViewModel : ViewModel() {

    var wifiManager: WifiManager? = null
    var ssid: String = ""
    var ipWebServer: String = "http://192.168.4.1"
    var Pot_ID: String = ""
    var timestamp: Long = 0
    val db = Firebase.database.reference
    var mAuth = FirebaseAuth.getInstance()
    var myPots = mutableMapOf<String, String>()

    var currentPot: String = ""
    var currentPlantType: String = ""
    var currentWifiSelection: String = ""
    var createPlantSelectedName: String = ""
    var emptyUserPots: Boolean? = null
    var choosePTModeOn: Boolean = false


    val userPots = mutableMapOf<String, PotData>()
    val plantTypes = mutableMapOf<String, PlantTypeData>()

    lateinit var googleSignInClient: GoogleSignInClient

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("226494663795-37g5amr4f0hffbji6c1k057td9tmv0lq.apps.googleusercontent.com")
        .requestEmail()
        .build()

    fun addUserPot(
        userPotSnapshot: DataSnapshot,
        potSnapshot: DataSnapshot,
        ptSnapshot: DataSnapshot,
        plantOverview: ViewPager2,
        mainLayout: ConstraintLayout,
        noPlantsLayout: ConstraintLayout
    ) {
        val newPot = createPotData(
            userPotSnapshot.key.toString(),
            userPotSnapshot.value.toString(),
            potSnapshot,
            ptSnapshot
        )
        userPots[newPot.id] = newPot
        if (emptyUserPots == null || emptyUserPots == true) {
            emptyUserPots = false
            updateHomeLayout(emptyUserPots, mainLayout, noPlantsLayout)
        }
        (plantOverview.adapter as PlantOverviewAdapter).submitList(userPots.values.toMutableList())
    }

    fun updateUserPot(potId: String, potName: String, plantOverview: ViewPager2) {
        if (!userPots.containsKey(potId)) {
            Log.w("updateUserPot", "Pot with id $potId not found in userPots")
            return
        }

        userPots[potId]!!.name = potName
        (plantOverview.adapter as PlantOverviewAdapter).submitList(userPots.values.toMutableList())
    }

    fun updateUserPot(
        potSnapshot: DataSnapshot,
        ptSnapshot: DataSnapshot,
        plantOverview: ViewPager2
    ) {
        val potId = potSnapshot.key.toString()

        if (!userPots.containsKey(potId)) {
            Log.w("updateUserPot", "Pot with id $potId not found in userPots")
            return
        }

        val newPot = createPotData(potId, userPots[potId]!!.name, potSnapshot, ptSnapshot)
        userPots[newPot.id] = newPot
        (plantOverview.adapter as PlantOverviewAdapter).submitList(userPots.values.toMutableList())
    }

    fun removeUserPot(
        potId: String,
        plantOverview: ViewPager2,
        mainLayout: ConstraintLayout,
        noPlantsLayout: ConstraintLayout
    ) {
        if (!userPots.containsKey(potId)) {
            Log.w("removeUserPot", "Pot with id $potId not found in userPots")
            return
        }

        userPots.remove(potId)
        if ((emptyUserPots == null || emptyUserPots == false) && userPots.isEmpty()) {
            emptyUserPots = true
            updateHomeLayout(emptyUserPots, mainLayout, noPlantsLayout)
        }
        (plantOverview.adapter as PlantOverviewAdapter).submitList(userPots.values.toMutableList())
    }

    private fun createPotData(
        potId: String,
        potName: String,
        potSnapshot: DataSnapshot,
        ptSnapshot: DataSnapshot,
    ): PotData {
        val manualMode = potSnapshot.child("Commands").child("Mode").value.toString() != "Humidity"

        val humidityThreshold =
            if (manualMode) (potSnapshot.child("Commands").child("Humidity").value as Long).toInt()
            else (ptSnapshot.child("humidity_threshold").value as Long).toInt()

        return PotData(
            potId,
            potName,
            ptSnapshot.child("name").value.toString(),
            ptSnapshot.child("img").value.toString(),
            manualMode,
            getConnectionStatusFromTimestamp(
                (potSnapshot.child("OnlineStatus").child("ConnectTime").value as Long).toInt()
            ),
            (potSnapshot.child("Humidity").child("LastHumidity").value as Long).toInt(),
            (potSnapshot.child("WaterLevel").value as Long).toInt(),
            (potSnapshot.child("Temperature").value as Long).toInt(),
            getLastWateringFromTimestamp(
                (potSnapshot.child("LastWatering").value as Long).toInt()
            ),
            potSnapshot.child("Commands").child("Mode").value.toString(),
            humidityThreshold
        )
    }
}