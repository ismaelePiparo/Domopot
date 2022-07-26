package com.example.domopotapp.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.domopotapp.R
import com.example.domopotapp.updateInputTextFocus
import com.google.android.material.bottomnavigation.BottomNavigationView


class ConfigStep2 : Fragment(R.layout.config_step_2_fragment) {

    //TAG è usato per identificare i debug nel menù Logcat
    private val TAG = ConfigStep2::class.java.name

    private val viewModel by activityViewModels<MainViewModel>()

    private var adapter: WifiSelectAdapter? = null
    private val availableWifiNetworks = mutableListOf<String>()

    private var results: List<ScanResult>? = null
    private var mySSID: String = ""
    private var myPWD: String = ""
    private var mRequestQueue: RequestQueue? = null
    private var initialScanWifi = true

    //private lateinit var wifiCardTitle: TextView
    private lateinit var loadingIcon: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)!!
        if (bottomNav.isVisible) bottomNav.visibility = View.GONE

        val connectBtn: Button = view.findViewById(R.id.configWifiConnectButton)
        val wifiCardPwdInput: EditText = view.findViewById(R.id.wifiCardPasswordInput)
        val wifiCardPwdLabel: TextView = view.findViewById(R.id.wifiCardPasswordLabel)
        val backButton: ImageButton = view.findViewById(R.id.configBackButton2)
        val wifiSelectRV: RecyclerView = view.findViewById(R.id.wifiSelectRV)

        val wifiCard: ConstraintLayout = view.findViewById(R.id.wifiCardLayout)
        val cardTitleBackButton: ImageButton = view.findViewById(R.id.cardTitleBackButton)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.configStep_2_to_home)
            }
        })

        //wifiCardTitle = view.findViewById(R.id.wifiCardTitle)
        loadingIcon = view.findViewById(R.id.configLoadingIcon)

        loadingIcon.animate().rotation(36000f).setDuration(30000).start()

        //wifiCardTitle.text = viewModel.ssid
        connectBtn.isEnabled = false

        adapter = WifiSelectAdapter(availableWifiNetworks, viewModel, wifiCard)
        wifiSelectRV.layoutManager = LinearLayoutManager(activity)
        wifiSelectRV.adapter = adapter

        backButton.setOnClickListener {
            findNavController().navigate(R.id.ConfigStep2_to_ConfigStep1)
        }

        cardTitleBackButton.setOnClickListener {
            wifiCard.visibility = View.GONE
        }

        connectBtn.setOnClickListener {
            //Spostare requestID() al passo precedente e sostituire con sendCredential()
            requestID();
        }

        wifiCardPwdInput.setOnFocusChangeListener { _: View, focused: Boolean ->
            updateInputTextFocus(wifiCardPwdInput, wifiCardPwdLabel, focused)
        }

        wifiCardPwdInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                connectBtn.isEnabled = !wifiCardPwdInput.text.isEmpty()
                mySSID= viewModel.currentWifiSelection
                myPWD = wifiCardPwdInput.text.toString()
            }
        })

        view.findViewById<Button>(R.id.config2TestNext).setOnClickListener { findNavController().navigate(R.id.ConfigStep2_to_ConfigStep3) }
    }

    private fun requestID() {
        //Toast.makeText(activity, "Invio dati in corso...", Toast.LENGTH_LONG).show()
        mRequestQueue = Volley.newRequestQueue(activity)

        val id_request: StringRequest = object : StringRequest(
            Method.GET, viewModel.ipWebServer,
            Response.Listener { response ->
                if (viewModel.Pot_ID.isNullOrEmpty()) {
                    viewModel.Pot_ID = response
                }

                sendCredentials()
            },
            Response.ErrorListener { error ->
                Log.i(TAG, "Error :$error")
                //wifiCardTitle.text = "Error"
            }) {}

        mRequestQueue?.add(id_request)
    }

    private fun sendCredentials() {
        //Toast.makeText(activity, "Invio credenziali in corso...", Toast.LENGTH_LONG).show()
        mRequestQueue = Volley.newRequestQueue(activity)

        val sr: StringRequest = object : StringRequest(
            Method.POST, viewModel.ipWebServer + "/credentials",
            Response.Listener { response ->
                viewModel.timestamp = System.currentTimeMillis() / 1000
                Log.w("Timestap ", viewModel.timestamp.toString())
                findNavController().navigate(R.id.ConfigStep2_to_ConfigStep3)
            },
            Response.ErrorListener { error ->
                Log.i(TAG, "Error :$error")
                //wifiCardTitle.text = "Error"
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["ssid"] = mySSID
                params["pass"] = myPWD
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/x-www-form-urlencoded"
                return params
            }
        }

        mRequestQueue?.add(sr)
        //findNavController().navigate(R.id.ConfigStep2_to_ConfigStep3)
    }


    //BroadcastReceveiver che notifica lo stato della connessione con l'esp
    var linkStatus: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!viewModel.wifiManager!!.connectionInfo.ssid.equals(viewModel.ssid)) {
                Toast.makeText(
                    activity,
                    "Si è verificato un errore nel collegamento con il vaso...",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigate(R.id.ConfigStep2_to_ConfigStep1)
            }
            if (!viewModel.wifiManager!!.isWifiEnabled) {
                Toast.makeText(
                    activity,
                    "Wifi scollegato! Accendere il Wifi...",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigate(R.id.ConfigStep2_to_ConfigStep1)
            }
        }
    }

    //Funzione che fa lo scanning delle reti WiFi
    private fun scanWifi() {
        availableWifiNetworks.clear()
        //availableWifiNetworks.add("Choose a Wifi Network...")
        //adapter!!.notifyDataSetChanged()
        adapter!!.submitList(availableWifiNetworks, loadingIcon)

        requireActivity().registerReceiver(
            wifiReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
        viewModel.wifiManager!!.startScan()
        //Toast.makeText(activity, "Scanning WiFi ...", Toast.LENGTH_SHORT).show()
        initialScanWifi = false
    }

    //Creazione di un Broadcast Receiver (Non fatto a lezione, preso online)
    var wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            results = viewModel.wifiManager!!.scanResults
            requireActivity().unregisterReceiver(this)

            for (scanResult in results!!) {
                var wifi_ssid = ""
                var wifi_ssid_first_nine_characters = ""

                if (scanResult.SSID.isEmpty()) {
                    wifi_ssid = "Rete Nascosta"
                } else {
                    wifi_ssid = scanResult.SSID
                }

                Log.d("WIFIScannerActivity", "WIFI SSID: $wifi_ssid")

                if (wifi_ssid.length > 8) {
                    wifi_ssid_first_nine_characters = wifi_ssid.substring(0, 9)
                } else {
                    wifi_ssid_first_nine_characters = wifi_ssid
                }
                Log.d("WIFIScannerActivity", "WIFI SSID 9: $wifi_ssid_first_nine_characters")
                Log.d(
                    "WIFIScannerActivity",
                    "scanResult.SSID: " + scanResult.SSID + ", scanResult.capabilities: " + scanResult.capabilities
                )

                if (wifi_ssid != "DomoPot_WiFi") availableWifiNetworks.add(wifi_ssid)
                //adapter!!.notifyDataSetChanged()
                adapter!!.submitList(availableWifiNetworks, loadingIcon)
            }


        }
    }

    override fun onResume() {
        super.onResume()
        if (initialScanWifi) {
            scanWifi()
        }
        //Inizializzazione dei BroadcastReceiver
        requireActivity().registerReceiver(
            linkStatus,
            IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        )
    }

    override fun onPause() {
        super.onPause()
        //Chiusura dei BroadcastReceiver
        requireActivity().unregisterReceiver(linkStatus)
    }

}