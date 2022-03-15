package com.example.domopotapp.ui.main

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

import com.example.domopotapp.R
import com.google.zxing.integration.android.IntentIntegrator

class ConfigStep1 : Fragment(R.layout.config_setp_1_fragment) {

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

    //private var wifiManager: WifiManager? = null
    private lateinit var scanBtn: Button
    private lateinit var nextBtn: Button
    private lateinit var wifiBtn: Button
    private lateinit var infoWifi: TextView
    private var defaultText:String="Sei connesso alla rete: "

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nextBtn = view.findViewById<Button>(R.id.next_1)
        infoWifi = view.findViewById<TextView>(R.id.current_wifi)
        scanBtn = view.findViewById<Button>(R.id.scanBtn)
        wifiBtn = view.findViewById<Button>(R.id.wifiMenu)
        viewModel.wifiManager = requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

        //Apre il menu del WiFi
        wifiBtn.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }
        scanBtn.setOnClickListener{
            startWifiQRCodeScanner(requireContext().applicationContext)
        }

        //Il bottone si abilita solo se si è connessi alla rete DomoPot_WiFi
        nextBtn.isEnabled = false
        nextBtn.setOnClickListener{
            findNavController().navigate(R.id.ConfigStep1_to_ConfigStep2)
        }

    }

    private fun startWifiQRCodeScanner(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val INTENT_ACTION_WIFI_QR_SCANNER = "android.settings.WIFI_DPP_ENROLLEE_QR_CODE_SCANNER"
            val wifiManager = context.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
            if (wifiManager.isEasyConnectSupported) {
                val intent = Intent(INTENT_ACTION_WIFI_QR_SCANNER)
                resultLauncher.launch(intent)
                //startActivity(intent)
                //startActivityForResult(intent, 5000)
            }
        }else{
            val intentIntegrator = IntentIntegrator(activity)
            intentIntegrator.setBeepEnabled(false)
            intentIntegrator.setCameraId(0)
            intentIntegrator.setPrompt("Scansiona il codice QR")
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            //intentIntegrator.initiateScan()
            resultLauncher.launch(intentIntegrator.createScanIntent())
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            Toast.makeText(activity, "Scanned: ", Toast.LENGTH_LONG).show()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                //connessione via software... da implementare
            }
        }else{
            Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show()

        }
    }

    //BroadcastReceveiver che notifica lo stato del Wifi (acceso/spento)
    var wifiStatus: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val wifiStateExtra = intent.getIntExtra(
                WifiManager.EXTRA_WIFI_STATE,
                WifiManager.WIFI_STATE_UNKNOWN
            )
            when (wifiStateExtra) {
                WifiManager.WIFI_STATE_ENABLED -> {
                    infoWifi.text = "Connessione in corso..."
                }
                WifiManager.WIFI_STATE_DISABLED -> {
                    infoWifi.text = "NO WIFI"
                }
            }
        }
    }

    //BroadcastReceveiver che notifica il cambio di rete Wifi
    var networkStatus: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if(viewModel.wifiManager!!.connectionInfo.ssid.equals("<unknown ssid>") && viewModel.wifiManager!!.isWifiEnabled){
                infoWifi.text = "Connessione in corso..."
            }else if (!viewModel.wifiManager!!.isWifiEnabled){
                infoWifi.text = "NO WIFI!!!"
            }else{
                infoWifi.text = defaultText + viewModel.wifiManager!!.connectionInfo.ssid
                if(viewModel.wifiManager!!.connectionInfo.ssid.equals("\"" + "DomoPot_WiFi" + "\"")){
                    nextBtn.isEnabled = true
                    viewModel.ssid = viewModel.wifiManager!!.connectionInfo.ssid
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //Inizializzazione dei BroadcastReceiver
        requireActivity().registerReceiver(wifiStatus, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        requireActivity().registerReceiver(networkStatus, IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION))
    }

    override fun onPause(){
        super.onPause()
        //Chiusura dei BroadcastReceiver
        requireActivity().unregisterReceiver(wifiStatus)
        requireActivity().unregisterReceiver(networkStatus)
    }


}