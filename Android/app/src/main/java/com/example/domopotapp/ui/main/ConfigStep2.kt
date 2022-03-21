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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.domopotapp.R


class ConfigStep2 : Fragment(R.layout.config_step_2_fragment) {

    //TAG è usato per identificare i debug nel menù Logcat
    private val TAG = ConfigStep2::class.java.name

    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var tv : TextView
    private lateinit var pwd : TextView
    private lateinit var connectBtn : Button
    private lateinit var back : Button
    private var results: List<ScanResult>? = null
    private val arrayList = arrayListOf<String>()
    lateinit var spinner : Spinner
    private var adapter : ArrayAdapter<*>? = null
    private var mySSID : String =""
    private var myPWD : String =""
    private var mRequestQueue: RequestQueue? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        back = view.findViewById<Button>(R.id.back_2)
        connectBtn = view.findViewById(R.id.conect)
        pwd = view.findViewById<TextView>(R.id.pwd)
        tv = view.findViewById<TextView>(R.id.textView)

        tv.text=viewModel.ssid
        connectBtn.isEnabled = false


        adapter = activity?.let { ArrayAdapter(it, R.layout.simple_list_item, arrayList) }
        spinner = view.findViewById<Spinner>(R.id.spinner)
        spinner.adapter=adapter
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                if(spinner.getPositionForView(view)==0){
                    pwd.isEnabled = false
                }else{
                    Toast.makeText(activity, "inserisci la password della rete: " + spinner.getItemAtPosition(i).toString(), Toast.LENGTH_LONG).show()
                    mySSID=spinner.getItemAtPosition(i).toString()
                    pwd.isEnabled = true
                }
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                return
            }
        })

        pwd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                connectBtn.isEnabled = !pwd.text.isEmpty()
                myPWD = pwd.text.toString()

            }
        })

        connectBtn.setOnClickListener{
            //Spostare requestID() al passo precedente e sostituire con sendCredential()
            requestID();
        }

        back.setOnClickListener{
            findNavController().navigate(R.id.ConfigStep2_to_ConfigStep1)
        }
    }

    private fun requestID() {

        Toast.makeText(activity, "Invio dati in corso...", Toast.LENGTH_LONG).show()
        mRequestQueue = Volley.newRequestQueue(activity)

        val id_request: StringRequest = object : StringRequest(
            Method.GET, viewModel.ipWebServer,
            Response.Listener { response ->
                if(viewModel.Pot_ID.isNullOrEmpty()){
                    viewModel.Pot_ID = response
                }
                sendCredentials()
            },
            Response.ErrorListener { error ->
                Log.i(TAG, "Error :$error")
                tv.text = "Error"
            }){}

        mRequestQueue?.add(id_request)
    }

    private fun sendCredentials() {

        Toast.makeText(activity, "Invio credenziali in corso...", Toast.LENGTH_LONG).show()
        mRequestQueue = Volley.newRequestQueue(activity)

        val sr: StringRequest = object : StringRequest(
            Method.POST, viewModel.ipWebServer + "/credentials",
            Response.Listener { response ->
                //findNavController().navigate(R.id.Step2_to_Step3)
            },
            Response.ErrorListener { error ->
                Log.i(TAG, "Error :$error")
                tv.text = "Error"
            })
        {
            @Throws(AuthFailureError::class)
            override fun getParams() : Map<String, String> {
                val params = HashMap<String,String>()
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
        findNavController().navigate(R.id.ConfigStep2_to_ConfigStep3)

    }


    //BroadcastReceveiver che notifica lo stato della connessione con l'esp
    var linkStatus: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if(!viewModel.wifiManager!!.connectionInfo.ssid.equals(viewModel.ssid)) {
                Toast.makeText(
                    activity,
                    "Si è verificato un errore nel collegamento con il vaso...",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigate(R.id.ConfigStep2_to_ConfigStep1)
            }
            if(!viewModel.wifiManager!!.isWifiEnabled) {
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
        arrayList.clear()
        arrayList.add("Choose a Wifi Network...")
        adapter!!.notifyDataSetChanged()

        requireActivity().registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        viewModel.wifiManager!!.startScan()
        Toast.makeText(activity, "Scanning WiFi ...", Toast.LENGTH_SHORT).show()
    }

    //Creazione di un Broadcast Receiver (Non fatto a lezione, preso online)
    var wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            results = viewModel.wifiManager!!.scanResults
            requireActivity().unregisterReceiver(this)

            for (scanResult in results!!) {
                var wifi_ssid = ""
                var wifi_ssid_first_nine_characters = ""

                if(scanResult.SSID.isEmpty()){
                    wifi_ssid = "Rete Nascosta"
                }else{
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

                arrayList.add(wifi_ssid)
                adapter!!.notifyDataSetChanged()
            }


        }
    }

    override fun onResume() {
        super.onResume()
        scanWifi()
        //Inizializzazione dei BroadcastReceiver
        requireActivity().registerReceiver(linkStatus, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
    }

    override fun onPause(){
        super.onPause()
        //Chiusura dei BroadcastReceiver
        requireActivity().unregisterReceiver(linkStatus)
    }

}