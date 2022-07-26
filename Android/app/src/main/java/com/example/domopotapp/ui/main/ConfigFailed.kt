package com.example.domopotapp.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R

class ConfigFailed : Fragment(R.layout.config_failed_fragment) {
    companion object {
        fun newInstance() = ConfigFailed()

        fun newInstanceWithBundle(b: Bundle): ConfigFailed {
            val f = ConfigFailed()
            f.arguments = b
            return f
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //disabilita il tasto back
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.w("BACK PRESSED","Attendere il completamento dell'operazione...")
            //Toast.makeText(activity, "Attendere il completamento dell'operazione...", Toast.LENGTH_LONG).show()

        }

        val backToHomeButton: Button = view.findViewById(R.id.configFailedBackToHomeBtn)
        val retryButton: Button = view.findViewById(R.id.configFailedRetryBtn)

        backToHomeButton.setOnClickListener {
            findNavController().navigate(R.id.action_configFailed_to_home2)
        }

        retryButton.setOnClickListener {
            findNavController().navigate(R.id.action_configFailed_to_configSetp_1)
        }
    }
}