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
        //valori usati per debug ---->
        //viewModel.Pot_ID = "DomoPot_01"
        //viewModel.timestamp = System.currentTimeMillis() / 1000

        ref = viewModel.db.child(viewModel.Pot_ID + "/OnlineStatus/ConnectTime")
        myListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connctionStatus = snapshot.value.toString()
                Log.w("connectionStatus: ",connctionStatus)
                if(connctionStatus.isNullOrEmpty()) {
                    tv.text = "Attendere..."
                    //getione del tempo di attesa
                    Log.w("start handler timer: ","timer start")
                    handler.postDelayed(runnable, 30000)
                }else if(!connctionStatus.isNullOrEmpty()){
                    if(connctionStatus.toLong()>viewModel.timestamp){
                        Log.w("stop handler timer: ","timer stop")
                        handler.removeCallbacks(runnable)
                        tv.text = "Associazione riuscita"

                        //inserisce l'ID del vaso e il nome nel database dell'utente
                        viewModel.mAuth.currentUser?.let {
                            viewModel.db.child("Users")
                                .child(it.uid)
                                .child("pots")
                                .child(viewModel.Pot_ID)
                                .setValue(viewModel.Pot_ID)
                                .addOnCompleteListener{
                                    if(it.isSuccessful){
                                        Log.w("Assegnato vaso a utente", "vaso assegnato correttamente")
                                        Toast.makeText(activity, "Associazione riuscita", Toast.LENGTH_LONG).show()
                                        ref.removeEventListener(myListener)

                                        findNavController().navigate(R.id.ConfigStep3_to_Home)
                                    }else{
                                        /*si potrebbe gestire il caso di un errore di scrittuta su DB
                                        e quindi disconnettere l'ESP da internet e riprovare l'associazione*/
                                    }
                                }

                        }
                    }else{
                        tv.text = "Attendere..."
                        //getione del tempo di attesa
                        Log.w("start handler timer: ","timer start")
                        handler.postDelayed(runnable, 30000)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        }

    }

    override fun onResume() {
        super.onResume()
        ref.addValueEventListener(myListener)

    }

    override fun onPause(){
        super.onPause()
        ref.removeEventListener(myListener)
        handler.removeCallbacks(runnable)

    }

    override fun onDestroy() {
        super.onDestroy()
        ref.removeEventListener(myListener)
        handler.removeCallbacks(runnable)

    }
}