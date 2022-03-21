package com.example.domopotapp.ui.main

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.domopotapp.R

class Settings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}