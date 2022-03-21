package com.example.domopotapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
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

    private lateinit var viewModel: MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }
}