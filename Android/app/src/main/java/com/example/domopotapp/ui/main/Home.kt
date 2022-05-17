package com.example.domopotapp.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.domopotapp.R
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


class Home : Fragment(R.layout.home_fragment) {
    companion object {
        fun newInstance() = Home()
        fun newInstanceWithBundle(b: Bundle): Home{
            val f = Home()
            f.arguments = b
            return f
        }
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var logoutBtn: Button
    private lateinit var plantName: TextView

    private lateinit var myListener: ValueEventListener
    private lateinit var ref: DatabaseReference
    private lateinit var user: FirebaseUser



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val add = view.findViewById<ImageButton>(R.id.add)

        logoutBtn = view.findViewById<Button>(R.id.logoutBtn)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        plantName = view.findViewById<TextView>(R.id.plantName)


        //DA GESTIRE LA PARTE DI CREAZIONE DELLE CARD PER I VASI DELL'UTENTE
        //QUESTO è SOLO UN TEST!!!
        user = viewModel.mAuth.currentUser!!
        ref = user?.let { viewModel.db.child("Users")
                                    .child(it.uid)
                                    .child("pots") }!!
        myListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val plants = snapshot.value.toString()
                if(plants == "0"){
                    Log.w("Caricamento vasi utente","NO PLANTS!")
                    plantName.text="No Plants!!!"
                }else{
                    snapshot.children.forEach{
                        Log.w("Caricamento vasi utente","---")
                            plantName.text=it.value.toString()
                            Log.w("Caricamento vasi utente",it.value.toString())
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("DB REsponse: ", "Failed to read value.", error.toException())
            }
        }

        add.setOnClickListener{
           findNavController().navigate(R.id.Home_to_ConfigStep1)
        }

        logoutBtn.setOnClickListener{
            signOut()
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
        ref.addValueEventListener(myListener)
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

