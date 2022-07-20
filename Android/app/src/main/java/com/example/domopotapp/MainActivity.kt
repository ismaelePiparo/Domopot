package com.example.domopotapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Verifica la versione di Android perchè dalla versione 10 servono dei permessi per usare WiFiManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE
                ), 0
            )
            //Permessi necessari da Android 10 in poi

        } else {
            //per le versioni precedenti (9, 8...) non serve nessun permesso particolare
        }

        val navigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val fragmentContainerView: FragmentContainerView = findViewById(R.id.fragmentContainerView)

        navigationView.selectedItemId = R.id.navigation_home

        navigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    fragmentContainerView.findNavController().navigate(R.id.action_global_home2)
                    true
                }
                R.id.navigation_guide -> {
                    fragmentContainerView.findNavController().navigate(R.id.action_global_guide)
                    true
                }
                R.id.navigation_settings -> {
                    fragmentContainerView.findNavController().navigate(R.id.action_global_settings)
                    true
                }
                else -> false
            }
        }
    }

}