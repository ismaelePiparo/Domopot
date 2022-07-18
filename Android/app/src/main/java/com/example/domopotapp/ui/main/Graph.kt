package com.example.domopotapp.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate


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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        currentPot = view.findViewById<TextView>(R.id.pot)
        currentPot.text = viewModel.myPots.getValue(viewModel.currentPot)

        backBtn = view.findViewById<Button>(R.id.back)
        backBtn.setOnClickListener{
            findNavController().navigate(R.id.graph_to_details)
        }

        val barChart = view.findViewById<BarChart>(R.id.barChart)

        //adding values
        val ourBarEntries: ArrayList<BarEntry> = ArrayList()
        var i = 0

        ourBarEntries.add(BarEntry(i.toFloat(), 10f))
        ourBarEntries.add(BarEntry((i+1).toFloat(), 8f))
        ourBarEntries.add(BarEntry((i+4).toFloat(), 1f))
        ourBarEntries.add(BarEntry((i+3).toFloat(), 4f))


        val barDataSet = BarDataSet(ourBarEntries, "")
        //set a template coloring
        barDataSet.setColors(Color.rgb(203, 203, 203))
        val data = BarData(barDataSet)
        barChart.data = data
        //setting the x-axis
        val xAxis: XAxis = barChart.xAxis
        //calling methods to hide x-axis gridlines
        barChart.axisLeft.setDrawGridLines(false)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove legend
        barChart.legend.isEnabled = false

        //remove description label
        barChart.description.isEnabled = false

        //add animation
        barChart.animateY(3000)
        //refresh the chart
        barChart.invalidate()
    }
}