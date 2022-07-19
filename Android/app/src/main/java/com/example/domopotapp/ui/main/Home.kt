package com.example.domopotapp.ui.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.domopotapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class Home : Fragment(R.layout.home_fragment) {
    companion object {
        fun newInstance() = Home()
        fun newInstanceWithBundle(b: Bundle): Home {
            val f = Home()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()

    //private lateinit var myPotsListener: ValueEventListener
    private lateinit var myPotsListener: ChildEventListener
    private lateinit var userRef: DatabaseReference
    private lateinit var potRef: DatabaseReference
    private lateinit var myPlantListener: ChildEventListener

    private lateinit var user: FirebaseUser

    var potsList: MutableList<PlantOverviewData> = mutableListOf()


    val images = listOf<String>("plant_img/peperomia.png", "plant_img/filodendro.png", "plant_img/peperomia.png", "plant_img/filodendro.png")


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)!!

        val addPlant = view.findViewById<ImageButton>(R.id.addPlantButton)
        val nextPlant = view.findViewById<ImageButton>(R.id.nextPlant)
        val prevPlant = view.findViewById<ImageButton>(R.id.prevPlant)
        val plantDetail = view.findViewById<Button>(R.id.plantDetails)

        user = viewModel.mAuth.currentUser!!

        if (!bottomNav.isVisible) bottomNav.visibility = View.VISIBLE

        // GESTICE IL CARICAMENTO DEI VASI DELL'UTENTE
        userRef = user.let {
            viewModel.db.child("Users")
                .child(it.uid)
                .child("pots")
        }

        val plantOverview: ViewPager2 = view.findViewById(R.id.plantOverview)
        val wormDotsIndicator = view.findViewById<DotsIndicator>(R.id.dotsIndicator)

        plantOverview.adapter = PlantOverviewAdapter(potsList)
        wormDotsIndicator.attachTo(plantOverview)

        // GESTIONE BUTTON
        addPlant.setOnClickListener {
            findNavController().navigate(R.id.Home_to_ConfigStep1)
        }

        plantDetail.setOnClickListener {
            findNavController().navigate(R.id.Home_to_details)
        }

        nextPlant.setOnClickListener {
            plantOverview.currentItem++
        }

        prevPlant.setOnClickListener {
            plantOverview.currentItem--
        }

        plantOverview.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.currentPot = potsList[position].id
                super.onPageSelected(position)
            }
        })

        myPotsListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.w("Vaso Aggiunto", "onChildAdded:" + snapshot.key!!)
                val pot = snapshot.key.toString()
                val name = snapshot.value.toString()

                val index = potsList.size

                viewModel.myPots[pot] = name
                Log.w("Map", viewModel.myPots.toString())

                if (index >= 0) {
                    val newData = PlantOverviewData(pot, name, images[index], 0, 0, 0, 0)
                    (plantOverview.adapter as PlantOverviewAdapter).addListItem(newData, index)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.w("Vaso Modificato", "onChildChanged:" + snapshot.key!!)
                val pot = snapshot.key.toString()
                val name = snapshot.value.toString()

                val index = potsList.indexOf(potsList.find { it.id == snapshot.key.toString() })
                Log.i("Index", index.toString())

                viewModel.myPots[pot] = name
                Log.w("Map", viewModel.myPots.toString())

                if (index >= 0) {
                    val newData = potsList[index]
                    newData.name = name
                    (plantOverview.adapter as PlantOverviewAdapter).updateListItem(newData, index)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.w("Vaso eliminato", "onChildRemoved:" + snapshot.key!!)
                val pot = snapshot.key.toString()

                val index = potsList.indexOf(potsList.find { it.id == snapshot.key.toString() })

                viewModel.myPots.remove(pot)
                Log.w("Map", viewModel.myPots.toString())

                if (index >= 0) (plantOverview.adapter as PlantOverviewAdapter).removeListItem(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ERRORE", "postComments:onCancelled", databaseError.toException())
                Toast.makeText(
                    context, "Failed to load comments.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        myPlantListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                updatePlantOverview(snapshot, plantOverview)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                updatePlantOverview(snapshot, plantOverview)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.w("ERRORE", "postComments:onCancelled", error.toException())
                Toast.makeText(
                    context, "Failed to load comments.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updatePlantOverview(potsSnapshot: DataSnapshot, plantOverview: ViewPager2) {
        val hum = potsSnapshot.child("Humidity").child("LastHumidity").value as Long
        Log.w("Humidity", hum.toString())

        val wL = potsSnapshot.child("WaterLevel").value as Long
        Log.w("Water Level", wL.toString())

        val index = potsList.indexOf(potsList.find { it.id == potsSnapshot.key.toString() })

        if (index >= 0) {
            val newData = potsList[index]
            newData.humidity = hum.toInt()
            newData.waterLevel = wL.toInt()
            (plantOverview.adapter as PlantOverviewAdapter).updateListItem(newData, index)
        }
    }

    override fun onResume() {
        super.onResume()
        potRef = viewModel.db.child("Pots")
        userRef.addChildEventListener(myPotsListener)
        potRef.addChildEventListener(myPlantListener)
    }

    override fun onPause() {
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