package com.example.domopotapp.ui.main

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

import com.example.domopotapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.client.result.ResultParser
import com.google.zxing.client.result.WifiParsedResult
import com.google.zxing.integration.android.IntentIntegrator

class ConfigStep1 : Fragment(R.layout.config_step_1_fragment) {

    companion object {
        fun newInstance() = ConfigStep1()

        fun newInstanceWithBundle(b: Bundle): ConfigStep1 {
            val f = ConfigStep1()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()

    private val TAG = ConfigStep1::class.java.name

    private lateinit var scanBtn: Button
    private lateinit var nextBtn: Button
    private lateinit var wifiBtn: Button
    private lateinit var infoWifi: TextView
    private var defaultText: String = "Sei connesso a: "

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)!!
        if (bottomNav.isVisible) bottomNav.visibility = View.GONE

        nextBtn = view.findViewById<Button>(R.id.next_1)
        infoWifi = view.findViewById<TextView>(R.id.current_wifi)
        scanBtn = view.findViewById<Button>(R.id.scanBtn)
        wifiBtn = view.findViewById<Button>(R.id.wifiMenu)
        viewModel.wifiManager = requireActivity().getApplicationContext()
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

        val backButton: ImageButton = view.findViewById(R.id.configBackButton1)

        //Apre il menu del WiFi
        wifiBtn.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }
        scanBtn.setOnClickListener {
            infoWifi.text = "Connessione in corso..."
            startWifiQRCodeScanner(requireContext().applicationContext)
        }

        //Il bottone si abilita solo se si è connessi alla rete DomoPot_WiFi
        // nextBtn.isEnabled = false
        nextBtn.setOnClickListener {
            //Sostituire con requestID() e controllare se il vaso sia presente nel DB
            //se presente tornare alla home altrimenti procedere con la configurazione
            findNavController().navigate(R.id.ConfigStep1_to_ConfigStep2)
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.ConfigStep1_to_Home)
        }

    }

    private fun startWifiQRCodeScanner(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val INTENT_ACTION_WIFI_QR_SCANNER = "android.settings.WIFI_DPP_ENROLLEE_QR_CODE_SCANNER"
            val wifiManager =
                context.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
            if (wifiManager.isEasyConnectSupported) {
                val intent = Intent(INTENT_ACTION_WIFI_QR_SCANNER)
                resultLauncher.launch(intent)
                //startActivity(intent)
                //startActivityForResult(intent, 5000)
            }
        } else {
            val intentIntegrator = IntentIntegrator(activity)
            intentIntegrator.setBeepEnabled(false)
            intentIntegrator.setCameraId(0)
            intentIntegrator.setPrompt("Scansiona il codice QR")
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            //intentIntegrator.initiateScan()
            resultLauncher.launch(intentIntegrator.createScanIntent())
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //Toast.makeText(activity, "Connessione in corso... ", Toast.LENGTH_LONG).show()
                } else {
                    val intentResult =
                        IntentIntegrator.parseActivityResult(result.resultCode, result.data)
                    val result = Result(intentResult.contents, null, null, BarcodeFormat.QR_CODE)
                    val parsedResult = ResultParser.parseResult(result) as WifiParsedResult
                    if (parsedResult.password.equals("\"" + "\"")) {
                        connectToWifi(parsedResult.ssid, "")
                    } else {
                        connectToWifi(parsedResult.ssid, parsedResult.password)
                    }

                    //Toast.makeText(activity, "Connessione in corso...", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(activity, "Operazione annullata", Toast.LENGTH_LONG).show()
            }
        }

    private fun connectToWifi(ssid: String, password: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                Log.e(TAG, "connection wifi pre Q")
                val wifiConfig = WifiConfiguration()
                wifiConfig.SSID = "\"" + ssid + "\""
                wifiConfig.preSharedKey = "\"" + password + "\""
                val netId = viewModel.wifiManager!!.addNetwork(wifiConfig)
                viewModel.wifiManager!!.disconnect()
                viewModel.wifiManager!!.enableNetwork(netId, true)
                viewModel.wifiManager!!.reconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Log.e(TAG, "connection wifi  Q")
            Log.e(TAG, "SSID: $ssid password: $password")
            val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build()
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build()
            val connectivityManager =
                context?.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager.bindProcessToNetwork(network)
                    Log.e(TAG, "onAvailable")
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    Log.e(TAG, "onLosing")
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.e(TAG, "losing active connection")
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Log.e(TAG, "onUnavailable")
                }
            }
            connectivityManager.requestNetwork(networkRequest, networkCallback)
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
                    infoWifi.setTextColor(resources.getColor(R.color.info))
                }
                WifiManager.WIFI_STATE_DISABLED -> {
                    infoWifi.text = "È necessario attivare il WiFi"
                    infoWifi.setTextColor(resources.getColor(R.color.danger))
                }
            }
        }
    }

    //BroadcastReceveiver che notifica il cambio di rete Wifi
    var networkStatus: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (viewModel.wifiManager!!.connectionInfo.ssid.equals("<unknown ssid>") && viewModel.wifiManager!!.isWifiEnabled) {
                infoWifi.text = "Connessione in corso..."
                infoWifi.setTextColor(resources.getColor(R.color.info))
            } else if (!viewModel.wifiManager!!.isWifiEnabled) {
                infoWifi.text = "È necessario attivare il WiFi"
                infoWifi.setTextColor(resources.getColor(R.color.danger))
            } else {
                //infoWifi.text = defaultText + viewModel.wifiManager!!.connectionInfo.ssid
                infoWifi.text = "Non sei connesso alla rete corretta"
                infoWifi.setTextColor(resources.getColor(R.color.danger))
                if (viewModel.wifiManager!!.connectionInfo.ssid.equals("\"" + "DomoPot_WiFi" + "\"")) {
                    infoWifi.setTextColor(resources.getColor(R.color.primary))
                    //nextBtn.isEnabled = true
                    viewModel.ssid = viewModel.wifiManager!!.connectionInfo.ssid
                    findNavController().navigate(R.id.ConfigStep1_to_ConfigStep2)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //Inizializzazione dei BroadcastReceiver
        requireActivity().registerReceiver(
            wifiStatus,
            IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        )
        requireActivity().registerReceiver(
            networkStatus,
            IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        )
    }

    override fun onPause() {
        super.onPause()
        //Chiusura dei BroadcastReceiver
        requireActivity().unregisterReceiver(wifiStatus)
        requireActivity().unregisterReceiver(networkStatus)
    }


}