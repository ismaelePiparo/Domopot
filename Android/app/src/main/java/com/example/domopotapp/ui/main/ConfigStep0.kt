package com.example.domopotapp.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ConfigStep0 : Fragment(R.layout.config_step_0_fragment) {
    companion object {
        fun newInstance() = Home()
        fun newInstanceWithBundle(b: Bundle): Home {
            val f = Home()
            f.arguments = b
            return f
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)!!
        if (bottomNav.isVisible) bottomNav.visibility = View.GONE

        val startButton: Button = view.findViewById(R.id.startButton)
        val backButton: ImageButton = view.findViewById(R.id.configBackButton0)

        startButton.setOnClickListener {
            findNavController().navigate(R.id.configStep0_to_configSetp_1)
        }
        backButton.setOnClickListener {
            findNavController().navigate(R.id.configStep0_to_home2)
        }
    }
}