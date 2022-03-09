package com.example.domopotapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv = view.findViewById<TextView>(R.id.potID)
        tv.text=viewModel.Pot_ID;

        val ref = viewModel.db.child(viewModel.Pot_ID+"/onLine")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val v = snapshot.getValue<Long>()
                if(v==null){
                    tv.text = "Attendere..."
                }else{
                    tv.text = "Associazione riuscita"
                    findNavController().navigate(R.id.ConfigStep3_to_Home)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}