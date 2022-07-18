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
        plantOverview.adapter = PlantOverviewAdapter(potsList)

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
                    val newData = PlantOverviewData(pot, name, "", 0, 0, 0, 0)
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

data class PlantOverviewData(
    var id: String,
    var name: String,
    var image: String,
    var humidity: Int,
    var waterLevel: Int,
    var temperature: Int,
    var lastWatering: Int
)

class PlantOverviewAdapter(private var l: MutableList<PlantOverviewData>) :
    RecyclerView.Adapter<PlantOverviewAdapter.PlantOverviewViewHolder>() {

    class PlantOverviewViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val plantName: TextView = v.findViewById(R.id.plantName)
        val humidity: TextView = v.findViewById(R.id.humidity)
        val temperature: TextView = v.findViewById(R.id.temperature)
        val waterLevel: TextView = v.findViewById(R.id.waterLevel)
        val lastWatering: TextView = v.findViewById(R.id.lastWatering)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantOverviewViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.plant_overview_item, parent, false)
        return PlantOverviewViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PlantOverviewViewHolder, position: Int) {
        holder.plantName.text = l[position].name
        holder.humidity.text = l[position].humidity.toString()
        holder.temperature.text = l[position].temperature.toString()
        holder.waterLevel.text = l[position].waterLevel.toString()
        holder.lastWatering.text = l[position].lastWatering.toString()
    }

    override fun getItemCount(): Int {
        return l.size
    }

    fun addListItem(newData: PlantOverviewData, position: Int) {
        l.add(position, newData)
        notifyItemInserted(position)
    }

    fun removeListItem(position: Int) {
        l.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateListItem(newData: PlantOverviewData, position: Int) {
        l[position].name = newData.name
        l[position].image = newData.image
        l[position].humidity = newData.humidity
        l[position].waterLevel = newData.waterLevel
        l[position].temperature = newData.temperature
        l[position].waterLevel = newData.waterLevel
        notifyItemChanged(position)
    }
}

