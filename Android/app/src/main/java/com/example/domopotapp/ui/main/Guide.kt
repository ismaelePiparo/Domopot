package com.example.domopotapp.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.domopotapp.R
import androidx.navigation.fragment.findNavController


class Guide : Fragment(R.layout.guide_fragment) {
    companion object {
        fun newInstance() = Guide()
        fun newInstanceWithBundle(b: Bundle): Guide{
            val f = Guide()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}