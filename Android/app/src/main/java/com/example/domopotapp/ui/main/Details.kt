package com.example.domopotapp.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
    private lateinit var type: TextView
    private lateinit var graphBtn: Button
    private lateinit var modeSwitch: Switch
    private lateinit var waterTag: TextView
    private lateinit var criteriLayout: ConstraintLayout



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        criteriLayout = view.findViewById(R.id.criteriLayout)
        criteriLayout.isVisible=false

        details = view.findViewById<TextView>(R.id.plantName)
        details.text = viewModel.myPots.getValue(viewModel.currentPot)
        type= view.findViewById<TextView>(R.id.plantType)
        //type.text= ???
        //details.text = viewModel.currentPot

        modeSwitch = view.findViewById(R.id.modeSwitch)
        modeSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                modeSwitch.setText("Automatica")
                criteriLayout.isVisible=false

            }
            else{
                modeSwitch.setText("Manuale")
                criteriLayout.isVisible=true

            }

            }


        waterTag=view.findViewById<TextView>(R.id.waterTag)
        //waterTag.text= ???
        graphBtn = view.findViewById<Button>(R.id.graph)

        graphBtn.setOnClickListener{
            findNavController().navigate(R.id.details_to_graph)
        }
    }
}