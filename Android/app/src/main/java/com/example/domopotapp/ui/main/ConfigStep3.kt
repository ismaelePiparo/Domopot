package com.example.domopotapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.example.domopotapp.R

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
    }
}