package com.example.domopotapp.ui.main

import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import java.time.LocalDateTime

class MainViewModel : ViewModel() {

    // TODO controllare i reset dei current

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
    var editPlantSelectedName: String = ""
    var emptyUserPots: Boolean? = null
    var choosePTModeOn: Boolean = false
    var choosePTAction: Int = R.id.action_guide_to_details

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

    @RequiresApi(Build.VERSION_CODES.M)
    fun updateUserPot(
        potSnapshot: DataSnapshot,
        ptSnapshot: DataSnapshot,
        fragment: Details
    ) {
        val potId = potSnapshot.key.toString()

        if (!userPots.containsKey(potId)) {
            Log.w("updateUserPot", "Pot with id $potId not found in userPots")
            return
        }

        val newPot = createPotData(potId, userPots[potId]!!.name, potSnapshot, ptSnapshot)
        userPots[newPot.id] = newPot

        fragment.updateView()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPotData(
        potId: String,
        potName: String,
        potSnapshot: DataSnapshot,
        ptSnapshot: DataSnapshot,
    ): PotData {
        val manualMode = !(potSnapshot.child("AutoMode").value as Boolean)

        val humidityThreshold =
            if (manualMode) (potSnapshot.child("Commands").child("Humidity").value as Long).toInt()
            else (ptSnapshot.child("humidity_threshold").value as Long).toInt()

        return PotData(
            potId,
            potName,
            ptSnapshot.key.toString(),
            ptSnapshot.child("img").value.toString(),
            manualMode,
            getConnectionStatusFromTimestamp(
                (potSnapshot.child("OnlineStatus").child("ConnectTime").value as Long).toInt()
            ),
            (potSnapshot.child("Humidity").child("LastHumidity").value as Long).toInt(),
            (potSnapshot.child("WaterLevel").value as Long).toInt(),
            (potSnapshot.child("Temperature").value as Long).toInt(),
            getLastWateringFromTimestamp(
                potSnapshot.child("LastWatering").value as Long
            ),
            potSnapshot.child("Commands").child("Mode").value.toString(),
            humidityThreshold,
            potSnapshot.child("Commands/Program/Timing").value as Long,
            (potSnapshot.child("Commands/Program/WaterQuantity").value as Long).toInt()
        )
    }

    fun uploadCurrentPot(
        id: String? = null,
        name: String? = null,
        type: String? = null,
        image: String? = null,

        manualMode: Boolean? = null,
        connectionStatus: Boolean? = null,

        humidity: Int? = null,
        waterLevel: Int? = null,
        temperature: Int? = null,
        lastWatering: LocalDateTime? = null,

        commandMode: String? = null,
        humidityThreshold: Int? = null,
        programTiming: Long? = null,
        waterQuantity: Int? = null,
    ) {
        // TODO aggiungere controlli
        val dbRef = db.child("Pots").child(currentPot)

        if (id != null) userPots[currentPot]!!.id = id
        if (name != null) userPots[currentPot]!!.name = name
        if (type != null) {
            userPots[currentPot]!!.type = type
            dbRef.child("PlantType").setValue(type)
        }
        if (image != null) userPots[currentPot]!!.image = image

        if (humidity != null) userPots[currentPot]!!.humidity = humidity
        if (waterLevel != null) userPots[currentPot]!!.waterLevel = waterLevel
        if (temperature != null) userPots[currentPot]!!.temperature = temperature
        if (lastWatering != null) userPots[currentPot]!!.lastWatering = lastWatering

        if (manualMode != null) {
            userPots[currentPot]!!.manualMode = manualMode
            dbRef.child("AutoMode").setValue(!manualMode)
        }
        if (connectionStatus != null) userPots[currentPot]!!.connectionStatus = connectionStatus

        if (commandMode != null) {
            userPots[currentPot]!!.commandMode = commandMode
            dbRef.child("Commands/Mode").setValue(commandMode)
        }
        if (humidityThreshold != null) {
            userPots[currentPot]!!.humidityThreshold = humidityThreshold
            dbRef.child("Commands/Humidity").setValue(humidityThreshold)
        }
        if (programTiming != null) {
            userPots[currentPot]!!.programTiming = programTiming
            dbRef.child("Commands/Program/Timing").setValue(programTiming)
        }
        if (waterQuantity != null) {
            userPots[currentPot]!!.waterQuantity = waterQuantity
            dbRef.child("Commands/Program/WaterQuantity").setValue(waterQuantity)
            dbRef.child("Commands/Immediate/WaterQuantity").setValue(waterQuantity)
        }
    }
}