package com.example.domopotapp.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class Settings : Fragment(R.layout.settings_fragment) {
    companion object {
        fun newInstance() = Settings()
        fun newInstanceWithBundle(b: Bundle): Settings{
            val f = Settings()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var bottomNav: BottomNavigationView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNav = activity?.findViewById(R.id.bottom_navigation)!!

        val logoutBtn = view.findViewById<Button>(R.id.logoutBtn)

        if (!bottomNav.isVisible) bottomNav.visibility = View.VISIBLE

        logoutBtn.setOnClickListener{
            signOut()
        }

    }

    override fun onResume() {
        super.onResume()
        bottomNav.menu.getItem(2).isChecked = true
    }

    private fun signOut() {
        viewModel.googleSignInClient = activity?.let { GoogleSignIn.getClient(it, viewModel.gso) }!!

        activity?.let {
            viewModel.googleSignInClient.signOut()
                .addOnCompleteListener(it, OnCompleteListener<Void?> {
                    Log.d("-------------------->","Log out")
                    viewModel.mAuth.signOut()
                    bottomNav.visibility = View.GONE
                    findNavController().navigate(R.id.action_settings_to_login)
                })
        }
    }
}