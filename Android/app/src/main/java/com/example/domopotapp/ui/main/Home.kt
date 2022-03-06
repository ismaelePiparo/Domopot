package com.example.domopotapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.example.domopotapp.R
import androidx.navigation.fragment.findNavController


class Home : Fragment(R.layout.fragment_home) {
    companion object {
        fun newInstance() = Home()
        fun newInstanceWithBundle(b: Bundle): Home{
            val f = Home()
            f.arguments = b
            return f
        }
    }

    private lateinit var viewModel: MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val add=view.findViewById<Button>(R.id.add)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        add.setOnClickListener{
           findNavController().navigate(R.id.action_home_to_configStep_1)
        }
    }}