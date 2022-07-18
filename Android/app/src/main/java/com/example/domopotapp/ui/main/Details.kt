package com.example.domopotapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R


class Details : Fragment(R.layout.details_fragment) {
    companion object {
        fun newInstance() = Guide()
        fun newInstanceWithBundle(b: Bundle): Guide{
            val f = Guide()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var details: TextView
    private lateinit var graphBtn: Button


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        details = view.findViewById<TextView>(R.id.potDetails)
        details.text = viewModel.myPots.getValue(viewModel.currentPot)

        graphBtn = view.findViewById<Button>(R.id.graph)

        graphBtn.setOnClickListener{
            findNavController().navigate(R.id.details_to_graph)
        }
    }
}