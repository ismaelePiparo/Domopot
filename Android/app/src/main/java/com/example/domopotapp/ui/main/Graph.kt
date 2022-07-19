package com.example.domopotapp.ui.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit


class Graph : Fragment(R.layout.graph_fragment) {
    companion object {
        fun newInstance() = Guide()
        fun newInstanceWithBundle(b: Bundle): Guide{
            val f = Guide()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var currentPot: TextView
    private lateinit var backBtn: Button

    private lateinit var potRef: DatabaseReference
    private lateinit var humidityListener: ValueEventListener


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        currentPot = view.findViewById<TextView>(R.id.pot)
        currentPot.text = viewModel.myPots.getValue(viewModel.currentPot)

        backBtn = view.findViewById<Button>(R.id.back)
        backBtn.setOnClickListener{
            findNavController().navigate(R.id.graph_to_details)
        }

        //Get values from db
        humidityListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    Log.w("Values", it.key.toString() + "___" + it.value.toString())
                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }


        //GRAFICO
        val barChart = view.findViewById<BarChart>(R.id.barChart)

        //adding values
        val ourBarEntries: ArrayList<BarEntry> = ArrayList()
        ourBarEntries.add(BarEntry(0f, 30f))
        ourBarEntries.add(BarEntry(1f, 80f))
        ourBarEntries.add(BarEntry(2f, 50f))
        ourBarEntries.add(BarEntry(3f, 40f))
        ourBarEntries.add(BarEntry(4f, 60f))

        val barDataSet = BarDataSet(ourBarEntries, "")
        //set a template coloring
        barDataSet.setColors(Color.rgb(203, 203, 203))
        //set label size
        barDataSet.valueTextSize = 15F
        //reformat label
        barDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "$value%"
            }
        }

        //setting the axis
        val xAxis: XAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setCenterAxisLabels(false)
        xAxis.granularity = 1f
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        barChart.axisLeft.setDrawAxisLine(true)
        barChart.axisLeft.setDrawGridLines(true)
        barChart.axisLeft.textSize =10f
        barChart.axisLeft.axisMaximum = 100f
        barChart.axisLeft.axisMinimum = 10f
        barChart.axisLeft.granularity = 2f
        barChart.axisRight.isEnabled = false

        //Create Label
        val xLabel  = ArrayList<String>()
        xLabel.add("uno")
        xLabel.add("due")
        xLabel.add("tre")
        xLabel.add("quattro")
        xLabel.add("cinque")

        //reformat axis value as label
        xAxis.textSize =15f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return xLabel[value.toInt()]
            }
        }

        //data
        val data = BarData(barDataSet)
        barChart.data = data

        //remove legend
        barChart.legend.isEnabled = false
        //remove description label
        barChart.description.isEnabled = false
        //fit bars
        barChart.setFitBars(true);
        //add animation
        barChart.animateY(3000)
        //refresh the chart
        barChart.invalidate()
        //set how many bars are visible
        barChart.setVisibleXRangeMaximum(4F)
        //set view to last bar
        barChart.moveViewToX(5F)

    }

    override fun onResume() {
        super.onResume()
        potRef = viewModel.db.child("Pots/" + viewModel.currentPot + "/Humidity/HistoryHumidity")
        potRef.addListenerForSingleValueEvent(humidityListener)
    }
}