package com.example.domopotapp.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.domopotapp.R
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class Home : Fragment(R.layout.home_fragment) {
    companion object {
        fun newInstance() = Home()
        fun newInstanceWithBundle(b: Bundle): Home{
            val f = Home()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var logoutBtn: Button
    private lateinit var plantName: TextView
    private lateinit var prevPlant: ImageButton
    private lateinit var nextPlant: ImageButton
    private lateinit var plantDetail: Button
    private lateinit var humidity: TextView
    private lateinit var waterLevel: TextView
    private lateinit var temperature: TextView
    private lateinit var lastWatering: TextView




    var pageCounter = 0;

    //private lateinit var myPotsListener: ValueEventListener
    private lateinit var myPotsListener: ChildEventListener
    private lateinit var userRef: DatabaseReference
    private lateinit var potRef: DatabaseReference
    private lateinit var myPlantListener: ValueEventListener

    private lateinit var user: FirebaseUser



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val add = view.findViewById<ImageButton>(R.id.addPlantButton)



        logoutBtn = view.findViewById<Button>(R.id.logoutBtn)
        plantName = view.findViewById<TextView>(R.id.plantName)
        nextPlant = view.findViewById<ImageButton>(R.id.nextPlant)
        prevPlant = view.findViewById<ImageButton>(R.id.prevPlant)
        humidity = view.findViewById<TextView>(R.id.humidity)
        waterLevel = view.findViewById<TextView>(R.id.waterLevel)
        plantDetail = view.findViewById<Button>(R.id.plantDetails)


        user = viewModel.mAuth.currentUser!!


        // GESTICE IL CARICAMENTO DEI VASI DELL'UTENTE
        userRef = user?.let { viewModel.db.child("Users")
                                    .child(it.uid)
                                    .child("pots") }!!
       /* myPotsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val plants = snapshot.value.toString()
                if(plants == "0"){
                    Log.w("Caricamento vasi utente","NO PLANTS!")
                    plantName.text="No Plants!!!"
                }else{
                    snapshot.children.forEach{
                        Log.w("Caricamento vasi utente","---")
                        var pot = it.key.toString()
                        var name = it.value.toString()
                        viewModel.myPots[pot] = name
                        Log.w("Map",viewModel.myPots.toString())
                        Log.w("Element in position 0",viewModel.myPots.keys.elementAt(0).toString())

                        Log.w("Caricamento vasi utente",it.value.toString())
                        updateView()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("DB REsponse: ", "Failed to read value.", error.toException())
            }
        }*/

        myPotsListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.w("Vaso Aggiunto", "onChildAdded:" + snapshot.key!!)
                var pot = snapshot.key.toString()
                var name = snapshot.value.toString()
                viewModel.myPots[pot] = name
                Log.w("Map", viewModel.myPots.toString())

                updateView(0)
        }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.w("Vaso Modificato", "onChildAdded:" + snapshot.key!!)
                var pot = snapshot.key.toString()
                var name = snapshot.value.toString()
                viewModel.myPots[pot] = name
                Log.w("Map", viewModel.myPots.toString())

                updateView(0)

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.w("Vaso eliminato", "onChildAdded:" + snapshot.key!!)
                var pot = snapshot.key.toString()
                viewModel.myPots.remove(pot)
                Log.w("Map", viewModel.myPots.toString())
                updateView(0)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ERRORE", "postComments:onCancelled", databaseError.toException())
                Toast.makeText(context, "Failed to load comments.",
                    Toast.LENGTH_SHORT).show()
            }
        }

        myPlantListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var hum = snapshot.child("Humidity").child("LastHumidity").value.toString()
                humidity.text = "Humidity: " + hum
                Log.w("Humidity", hum.toString())
                var wL = snapshot.child("WaterLevel").value.toString()
                waterLevel.text = "W level : " + wL
                Log.w("Water Level", wL.toString())
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("ERRORE", "postComments:onCancelled", error.toException())
                Toast.makeText(context, "Failed to load comments.",
                    Toast.LENGTH_SHORT).show()
            }
        }


        // GESTIONE BUTTON
        add.setOnClickListener{
           findNavController().navigate(R.id.Home_to_ConfigStep1)
        }

        nextPlant.setOnClickListener{
            if(pageCounter < viewModel.myPots.size - 1){
                pageCounter++
            }
            else pageCounter = 0

            updateView(pageCounter)
        }

        prevPlant.setOnClickListener{
            if(pageCounter > 0){
                pageCounter--
            }
            else pageCounter = viewModel.myPots.size - 1

            updateView(pageCounter)
        }

        logoutBtn.setOnClickListener{
            signOut()
        }

        plantDetail.setOnClickListener{
            findNavController().navigate(R.id.Home_to_details)

        }
    }


    // AGGIORNA LA VISTA
    private fun updateView(page: Int){

        potRef.removeEventListener(myPlantListener)
        Log.w("UpdateView", "page "+ page.toString())
        if(viewModel.myPots.isEmpty()){
            plantName.text="No Pots!!!"
        }else{
            viewModel.currentPot = viewModel.myPots.keys.elementAt(page)
            plantName.text=viewModel.myPots.values.elementAt(page)
            potRef = viewModel.db.child("Pots")
                .child(viewModel.currentPot)
            potRef.addValueEventListener(myPlantListener)
            Log.w("Current pot", viewModel.currentPot)
        }
    }

    private fun signOut() {
        viewModel.googleSignInClient = activity?.let { GoogleSignIn.getClient(it, viewModel.gso) }!!

        activity?.let {
            viewModel.googleSignInClient.signOut()
                .addOnCompleteListener(it, OnCompleteListener<Void?> {
                    Log.d("-------------------->","Log out")
                    viewModel.mAuth.signOut()
                    findNavController().navigate(R.id.home_to_login)
                })
        }
    }

    override fun onResume() {
        super.onResume()
        potRef = viewModel.db.child("Pots")
        userRef.addChildEventListener(myPotsListener)
    }

    override fun onPause(){
        super.onPause()
        userRef.removeEventListener(myPotsListener)
        potRef.removeEventListener(myPlantListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        userRef.removeEventListener(myPotsListener)
        potRef.removeEventListener(myPlantListener)
    }
}

