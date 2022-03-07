package com.example.domopotapp.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
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

    private val viewModel by activityViewModels<MainViewModel>()

    private val TAG = ConfigStep1::class.java.name

    private var wifiManager: WifiManager? = null
    private lateinit var scanBtn: Button
    private lateinit var nextBtn: Button
    private lateinit var wifiBtn: Button
    private lateinit var infoWifi: TextView
    private var currentWifi: String =""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nextBtn=view.findViewById<Button>(R.id.next)
        infoWifi=view.findViewById<TextView>(R.id.current_wifi)
        scanBtn=view.findViewById<Button>(R.id.scanBtn)
        wifiBtn=view.findViewById<Button>(R.id.wifiMenu)
        wifiManager = requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiBtn.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }

        nextBtn.isEnabled=false
        nextBtn.setOnClickListener{
            findNavController().navigate(R.id.action_configStep_1_to_configStep_2)
        }
    }
    var wifiStatus: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val wifiStateExtra = intent.getIntExtra(
                WifiManager.EXTRA_WIFI_STATE,
                WifiManager.WIFI_STATE_UNKNOWN
            )
            when (wifiStateExtra) {
                WifiManager.WIFI_STATE_ENABLED -> {
                    infoWifi.text="connessione in corso..."
                }
                WifiManager.WIFI_STATE_DISABLED -> {
                    infoWifi.text="NO WIFI"
                }
            }
        }
    }

    var networkStatus: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if(wifiManager!!.connectionInfo.ssid.equals("<unknown ssid>") && wifiManager!!.isWifiEnabled){
                infoWifi.text = "connessione in corso..."
            }else if (!wifiManager!!.isWifiEnabled){
                infoWifi.text="NO WIFI"
            }else{
                infoWifi.text=wifiManager!!.connectionInfo.ssid
                if(wifiManager!!.connectionInfo.ssid.equals("\"" + "AndroidWifi" + "\"")){
                    nextBtn.isEnabled=true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(wifiStatus, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        requireActivity().registerReceiver(networkStatus, IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION))
    }

    override fun onPause(){
        super.onPause()
        requireActivity().unregisterReceiver(wifiStatus)
        requireActivity().unregisterReceiver(networkStatus)
    }
}