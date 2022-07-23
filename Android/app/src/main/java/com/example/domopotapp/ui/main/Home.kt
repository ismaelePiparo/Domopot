package com.example.domopotapp.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.domopotapp.R
import com.example.domopotapp.defaultFirebaseOnFailureListener
import com.example.domopotapp.updateHomeLayout
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

    private lateinit var userPotsRef: DatabaseReference
    private lateinit var globalPotsRef: DatabaseReference
    private lateinit var userPotsListener: ChildEventListener
    private lateinit var globalPotsListener: ChildEventListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)!!

        val mainLayout: ConstraintLayout = view.findViewById(R.id.main)
        val noPlantsLayout: ConstraintLayout = view.findViewById(R.id.noPlantsLayout)
        val loadingIcon: ImageView = view.findViewById(R.id.loadingIcon)

        val addPlant = view.findViewById<ImageButton>(R.id.addPlantButton)
        val bigAddPlant = view.findViewById<ImageButton>(R.id.bigAddPlantButton)
        val nextPlant = view.findViewById<ImageButton>(R.id.nextPlant)
        val prevPlant = view.findViewById<ImageButton>(R.id.prevPlant)
        val plantDetail = view.findViewById<Button>(R.id.plantDetails)
        val plantOverview: ViewPager2 = view.findViewById(R.id.plantOverview)
        val dotsIndicator = view.findViewById<DotsIndicator>(R.id.dotsIndicator)

        viewModel.currentPlantType = ""

        if (!bottomNav.isVisible) bottomNav.visibility = View.VISIBLE
        loadingIcon.animate().rotation(36000f).setDuration(30000).start()
        updateHomeLayout(viewModel.emptyUserPots, mainLayout, noPlantsLayout)

        plantOverview.adapter = PlantOverviewAdapter(viewModel.userPots.values.toMutableList(), viewModel)
        dotsIndicator.attachTo(plantOverview)

        // GESTIONE BUTTON
        addPlant.setOnClickListener {
            findNavController().navigate(R.id.home2_to_configStep0)
        }
        bigAddPlant.setOnClickListener {
            findNavController().navigate(R.id.home2_to_configStep0)
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

        userPotsRef = viewModel.mAuth.currentUser!!.let {
            viewModel.db.child("Users")
                .child(it.uid)
                .child("pots")
        }
        globalPotsRef = viewModel.db.child("Pots")

        if (viewModel.emptyUserPots == null) {
            userPotsRef.get().addOnSuccessListener {
                viewModel.emptyUserPots = it.childrenCount.toInt() == 0
                updateHomeLayout(viewModel.emptyUserPots, mainLayout, noPlantsLayout)
            }
        }

        userPotsListener = UserPotsListener(viewModel, plantOverview, mainLayout, noPlantsLayout)
        globalPotsListener = GlobalPotsListener(viewModel, plantOverview)

        userPotsRef.addChildEventListener(userPotsListener)
        globalPotsRef.addChildEventListener(globalPotsListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        userPotsRef.removeEventListener(userPotsListener)
        globalPotsRef.removeEventListener(globalPotsListener)
    }
}