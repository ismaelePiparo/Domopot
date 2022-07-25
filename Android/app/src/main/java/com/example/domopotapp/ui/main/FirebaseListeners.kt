package com.example.domopotapp.ui.main

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.domopotapp.defaultFirebaseOnFailureListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class UserPotsListener(
    private val viewModel: MainViewModel,
    private val plantOverview: ViewPager2,
    private val mainLayout: ConstraintLayout,
    private val noPlantsLayout: ConstraintLayout
) : ChildEventListener {
    override fun onChildAdded(userPotSnapshot: DataSnapshot, previousChildName: String?) {
        Log.w("Vaso Aggiunto", "onChildAdded:" + userPotSnapshot.key!!)
        val potId = userPotSnapshot.key.toString()

        if (viewModel.userPots.containsKey(potId)) viewModel.userPots[potId]!!.name =
            userPotSnapshot.value.toString()
        else {
            viewModel.db.child("Pots").child(potId).get()
                .addOnSuccessListener { potSnapshot ->

                    Log.i("firebase", "Got pot data from: ${potSnapshot.key}")
                    val plantType = potSnapshot.child("PlantType").value.toString()

                    viewModel.db.child("PlantTypes").child(plantType).get()
                        .addOnSuccessListener { ptSnapshot ->

                            Log.i("firebase", "Got plant type data from: ${ptSnapshot.key}")
                            viewModel.addUserPot(
                                userPotSnapshot,
                                potSnapshot,
                                ptSnapshot,
                                plantOverview,
                                mainLayout,
                                noPlantsLayout
                            )

                        }.addOnFailureListener { defaultFirebaseOnFailureListener }
                }.addOnFailureListener { defaultFirebaseOnFailureListener }
        }



        viewModel.myPots[potId] = userPotSnapshot.value.toString()
        Log.w("Map", viewModel.myPots.toString())
    }

    override fun onChildChanged(userPotSnapshot: DataSnapshot, previousChildName: String?) {
        Log.w("Vaso Modificato", "onChildChanged:" + userPotSnapshot.key!!)
        val potId = userPotSnapshot.key.toString()
        val potName = userPotSnapshot.value.toString()

        if (viewModel.userPots.containsKey(potId)) viewModel.updateUserPot(
            potId,
            potName,
            plantOverview
        )



        viewModel.myPots[potId] = potName
        Log.w("Map", viewModel.myPots.toString())
    }

    override fun onChildRemoved(userPotSnapshot: DataSnapshot) {
        Log.w("Vaso eliminato", "onChildRemoved:" + userPotSnapshot.key!!)
        val potId = userPotSnapshot.key.toString()

        if (viewModel.userPots.containsKey(potId)) viewModel.removeUserPot(
            potId,
            plantOverview,
            mainLayout,
            noPlantsLayout
        )



        viewModel.myPots.remove(potId)
        Log.w("Map", viewModel.myPots.toString())
    }

    override fun onChildMoved(userPotSnapshot: DataSnapshot, previousChildName: String?) {}

    override fun onCancelled(databaseError: DatabaseError) {
        Log.w("ERRORE", "postComments:onCancelled", databaseError.toException())
    }
}

class GlobalPotsListener(
    private val viewModel: MainViewModel,
    private val plantOverview: ViewPager2
) : ChildEventListener {
    override fun onChildAdded(potSnapshot: DataSnapshot, previousChildName: String?) {}

    override fun onChildChanged(potSnapshot: DataSnapshot, previousChildName: String?) {
        val potId = potSnapshot.key.toString()

        if (viewModel.userPots.containsKey(potId)) {
            val plantType = potSnapshot.child("PlantType").value.toString()
            viewModel.db.child("PlantTypes").child(plantType).get()
                .addOnSuccessListener { ptSnapshot ->
                    Log.i("firebase", "Got plant type data from: ${ptSnapshot.key}")

                    viewModel.updateUserPot(potSnapshot, ptSnapshot, plantOverview)
                }.addOnFailureListener { defaultFirebaseOnFailureListener }
        }
    }

    override fun onChildRemoved(potSnapshot: DataSnapshot) {}

    override fun onChildMoved(potSnapshot: DataSnapshot, previousChildName: String?) {}

    override fun onCancelled(error: DatabaseError) {
        Log.w("ERRORE", "postComments:onCancelled", error.toException())
    }
}

class SinglePotListener(
    private val viewModel: MainViewModel,
    private val fragment: Details
) : ChildEventListener {
    override fun onChildAdded(potSnapshot: DataSnapshot, previousChildName: String?) {}

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onChildChanged(potSnapshot: DataSnapshot, previousChildName: String?) {
        val potId = potSnapshot.key.toString()

        if (viewModel.currentPot == potId) {
            val plantType = potSnapshot.child("PlantType").value.toString()
            viewModel.db.child("PlantTypes").child(plantType).get()
                .addOnSuccessListener { ptSnapshot ->
                    Log.i("firebase", "Got plant type data from: ${ptSnapshot.key}")

                    viewModel.updateUserPot(potSnapshot, ptSnapshot, fragment)
                }.addOnFailureListener { defaultFirebaseOnFailureListener }
        }
    }

    override fun onChildRemoved(potSnapshot: DataSnapshot) {}

    override fun onChildMoved(potSnapshot: DataSnapshot, previousChildName: String?) {}

    override fun onCancelled(error: DatabaseError) {
        Log.w("ERRORE", "postComments:onCancelled", error.toException())
    }
}