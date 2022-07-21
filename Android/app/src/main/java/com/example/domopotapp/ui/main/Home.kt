package com.example.domopotapp.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.domopotapp.R
import com.example.domopotapp.defaultFirebaseOnFailureListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
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

    private lateinit var userRef: DatabaseReference
    private lateinit var potRef: DatabaseReference
    private lateinit var userPotsListener: ChildEventListener
    private lateinit var globalPotsListener: ChildEventListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)!!

        val addPlant = view.findViewById<ImageButton>(R.id.addPlantButton)
        val nextPlant = view.findViewById<ImageButton>(R.id.nextPlant)
        val prevPlant = view.findViewById<ImageButton>(R.id.prevPlant)
        val plantDetail = view.findViewById<Button>(R.id.plantDetails)

        if (!bottomNav.isVisible) bottomNav.visibility = View.VISIBLE

        val plantOverview: ViewPager2 = view.findViewById(R.id.plantOverview)
        val dotsIndicator = view.findViewById<DotsIndicator>(R.id.dotsIndicator)

        plantOverview.adapter = PlantOverviewAdapter(viewModel.userPots.values.toMutableList())
        dotsIndicator.attachTo(plantOverview)

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
                viewModel.currentPot =
                    (plantOverview.adapter as PlantOverviewAdapter).l[position].id
                super.onPageSelected(position)
            }
        })

        userRef = viewModel.mAuth.currentUser!!.let {
            viewModel.db.child("Users")
                .child(it.uid)
                .child("pots")
        }
        potRef = viewModel.db.child("Pots")

        userPotsListener = object : ChildEventListener {
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

                            viewModel.addUserPot(
                                userPotSnapshot,
                                potSnapshot,
                                viewModel.plantTypes[plantType]!!,
                                plantOverview
                            )
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
                    plantOverview
                )



                viewModel.myPots.remove(potId)
                Log.w("Map", viewModel.myPots.toString())
            }

            override fun onChildMoved(userPotSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ERRORE", "postComments:onCancelled", databaseError.toException())
            }
        }

        globalPotsListener = object : ChildEventListener {
            override fun onChildAdded(potSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(potSnapshot: DataSnapshot, previousChildName: String?) {
                val potId = potSnapshot.key.toString()

                if (viewModel.userPots.containsKey(potId)) {
                    val plantType = potSnapshot.child("PlantType").value.toString()
                    viewModel.updateUserPot(
                        potSnapshot,
                        viewModel.plantTypes[plantType]!!,
                        plantOverview
                    )
                }
            }

            override fun onChildRemoved(potSnapshot: DataSnapshot) {}

            override fun onChildMoved(potSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.w("ERRORE", "postComments:onCancelled", error.toException())
            }
        }

        userRef.addChildEventListener(userPotsListener)
        potRef.addChildEventListener(globalPotsListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        userRef.removeEventListener(userPotsListener)
        potRef.removeEventListener(globalPotsListener)
    }
}