package com.example.domopotapp.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.lifecycle.ViewModelProvider
import com.example.domopotapp.R
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.OnCompleteListener


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val add = view.findViewById<ImageButton>(R.id.add)

        logoutBtn = view.findViewById<Button>(R.id.logoutBtn)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

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
}