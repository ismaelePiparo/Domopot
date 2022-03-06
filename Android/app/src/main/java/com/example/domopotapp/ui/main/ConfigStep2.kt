package com.example.domopotapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R


class ConfigStep2 : Fragment(R.layout.fragment_config_step_2) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val back = view.findViewById<Button>(R.id.back)
        back.setOnClickListener{
            findNavController().navigate(R.id.action_configStep_2_to_configStep_1)
        }
    }

}