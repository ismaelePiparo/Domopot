package com.example.domopotapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.navigation.fragment.findNavController

import com.example.domopotapp.R

class ConfigStep1 : Fragment(R.layout.fragment_config_setp_1) {
    companion object {
        fun newInstance() = ConfigStep1()
        fun newInstanceWithBundle(b: Bundle): ConfigStep1{
            val f = ConfigStep1()
            f.arguments = b
            return f
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val next=view.findViewById<Button>(R.id.add)
        val back=view.findViewById<Button>(R.id.back)

        next.setOnClickListener{
            findNavController().navigate(R.id.action_configStep_1_to_configStep_2)
        }

        back.setOnClickListener{
            findNavController().navigate(R.id.action_configStep_1_to_home)
        }

    }
}