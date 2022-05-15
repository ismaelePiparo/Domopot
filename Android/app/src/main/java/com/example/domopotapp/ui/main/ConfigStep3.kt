package com.example.domopotapp.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class ConfigStep3 : Fragment(R.layout.config_step_3_fragment) {



    companion object {
        fun newInstance() = ConfigStep1()

        fun newInstanceWithBundle(b: Bundle): ConfigStep1{
            val f = ConfigStep1()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var tv : TextView
    private lateinit var ref: DatabaseReference
    private lateinit var myListener: ValueEventListener
    private var handler = Handler(Looper.getMainLooper())
    private var runnable = Runnable { Log.w("HANDLER: ","Something went wrong!")
        Toast.makeText(activity, "Errore nel collegamento! Riprovare...", Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.ConfigStep3_to_Home) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //disabilita il tasto back
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.w("BACK PRESSED","Attendere il completamento dell'operazione...")
            Toast.makeText(activity, "Attendere il completamento dell'operazione...", Toast.LENGTH_LONG).show()

        }


        tv = view.findViewById<TextView>(R.id.potID)
        tv.text = "Carimento...";
        //usato per debug ----> viewModel.Pot_ID = "DomoPot_01"
        ref = viewModel.db.child(viewModel.Pot_ID + "/OnlineStatus/ConnectTime")
        myListener = ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val v = snapshot.value.toString()
                Log.w("Value from DB: ",v.toString())
                if(v.isNullOrEmpty()) {
                    tv.text = "Attendere..."
                    //getione del tempo di attesa
                    handler.postDelayed(runnable, 10000)
                }else if(!v.isNullOrEmpty()){
                    handler.removeCallbacks(runnable)
                    tv.text = "Associazione riuscita"

                    //inserisce l'ID del vaso e il nome nel database dell'utente
                    val pot: MutableMap<String, Any> = HashMap()
                    pot["PotID"] = viewModel.Pot_ID!!
                    pot["PotName"] = viewModel.Pot_ID!!
                    viewModel.mAuth.currentUser?.let {
                        viewModel.db.child("Users")
                            .child(it.uid).child("pots").setValue(pot)}
                    Toast.makeText(activity, "Associazione riuscita", Toast.LENGTH_LONG).show()

                    findNavController().navigate(R.id.ConfigStep3_to_Home)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause(){
        super.onPause()
        ref.removeEventListener(myListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        ref.removeEventListener(myListener)
    }
}