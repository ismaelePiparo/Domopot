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
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList


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
    private lateinit var rangeBtn: Button

    private lateinit var potRef: DatabaseReference
    private lateinit var humidityListener: ValueEventListener

    val humMap = mutableMapOf<String, Float>()
    var range = 1


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        currentPot = view.findViewById<TextView>(R.id.pot)
        currentPot.text = viewModel.myPots.getValue(viewModel.currentPot)

        val barChart = view.findViewById<BarChart>(R.id.barChart)

        backBtn = view.findViewById<Button>(R.id.back)
        backBtn.setOnClickListener{
            findNavController().navigate(R.id.graph_to_details)
        }

        rangeBtn = view.findViewById<Button>(R.id.range)
        rangeBtn.setOnClickListener{

            range = 2


            rangeBtn.text = range.toString()+"h"
            generateGraph(barChart)
        }

        //Get values from db
        humidityListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //snapshot.children.forEach{
                //    Log.w("Values", it.key.toString() + "___" + it.value.toString())
                //}
                var counter = 0
                for (item in snapshot.children.reversed()){
                    Log.w("Values", item.key.toString() + "___" + item.value.toString())
                    humMap[item.key.toString()] = item.value.toString().toFloat()
                    counter++
                    if (counter==5){
                        for ((key, value) in humMap) {
                            Log.w("map", "$key = $value")
                        }
                        generateGraph(barChart)
                        break
                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
    }

    //FUNZIONE PER CREARE IL GRAFICO
    private fun generateGraph(barChart: BarChart) {

        //adding values
        var barCounter = 0
        var itemCounter = 0
        val ourBarEntries: ArrayList<BarEntry> = ArrayList()
        //Create Label Array
        val xLabel  = ArrayList<String>()

        var meanCounter = 0
        var humidityMean = 0
        var startHour = ""
        var timestampHour = ""
        val sdfhour = java.text.SimpleDateFormat("HH")
        var date : Date
        var hourFrom =""
        var hourTo =""

        date = java.util.Date(humMap.toSortedMap(compareBy { it }).firstKey().toLong() * 1000)
        startHour = sdfhour.format(date).toString()
        Log.w("start hour",startHour.toString())
        var intervalHour = arrayOf<String>("00", "01", "02","03","04","05","06","07","08","09","10"
            ,"11","12","13","14","15","16","17","18","19","20","21","22","23")
        for (hour in intervalHour.indices step range) {
            hourFrom = intervalHour[hour];
            if(hour+range >= intervalHour.size){
                hourTo="24"
            }else{
                hourTo=intervalHour[hour+range]
            }
            Log.w("interval", "from $hourFrom, to $hourTo")
            meanCounter = 0
            humidityMean = 0
            for ((key, value) in humMap.toSortedMap(compareBy { it })) {
                date = java.util.Date(key.toLong() * 1000)
                timestampHour = sdfhour.format(date).toString()

                if (timestampHour.toInt() < hourTo.toInt() && timestampHour.toInt() >= hourFrom.toInt()) {
                    humidityMean += value.toInt()
                    meanCounter++
                    Log.w("media if", "$humidityMean $meanCounter ${humidityMean/meanCounter}")
                }else{
                    Log.w("ALLERT:","no DATA in this interval" )
                }
            }
            xLabel.add("$hourFrom:00/$hourTo:00")

            if(meanCounter>0){
                var mean = humidityMean/meanCounter
                ourBarEntries.add(BarEntry(barCounter.toFloat(), mean.toFloat()))
                barCounter++
            }else{
                ourBarEntries.add(BarEntry(barCounter.toFloat(), 0f))
                barCounter++
            }

        }


        /*var intervalHour = arrayOf<String>("00", "01", "02","03","04","05","06","07","08","09","10"
            ,"11","12","13","14","15","16","17","18","19","20","21","22","23")
        for (hour in intervalHour.indices step range){
            hourFrom = intervalHour[hour];
            if(hour+range >= intervalHour.size){
                hourTo=intervalHour[0]
            }else{
                hourTo=intervalHour[hour+range]
            }
            xLabel.add("$hourFrom:00/$hourTo:00")


            Log.w("interval", "from $hourFrom, to $hourTo")
            for ((key, value) in humMap) {}
        }

        hourFrom=""
        hourTo=""


        //__________________________________________________________________
        */
        //Log.w("map", humMap.toString())
        //Log.w("mapRev", humMap.toSortedMap(compareBy { it }).toString())

        /*for ((key, value) in humMap.toSortedMap(compareBy { it })) {
            date = java.util.Date(key.toLong() * 1000)
            Log.w("map item:",key.toString() +" -> "+value.toString() )
            timestampHour = sdfhour.format(date).toString()


            if (hourFrom == ""){
                hourFrom = timestampHour
                hourTo = sdfhour.format(java.util.Date((key.toLong() + (range *60*60)) * 1000)).toString()
            }
            Log.w("->","from $hourFrom, to $hourTo, timestamp $timestampHour" )

            if (timestampHour.toInt() < hourTo.toInt() && timestampHour.toInt() >= hourFrom.toInt()) {
                humidityMean += value.toInt()
                meanCounter++
                Log.w("media if", "$humidityMean $meanCounter ${humidityMean/meanCounter}")
            }else{
                xLabel.add("$hourFrom:00/$hourTo:00")
                var mean = humidityMean/meanCounter
                ourBarEntries.add(BarEntry(barCounter.toFloat(), mean.toFloat()))

                hourFrom = timestampHour
                hourTo = sdfhour.format(java.util.Date((key.toLong() + (range *60*60)) * 1000)).toString()
                Log.w("media else", "$humidityMean $meanCounter ${humidityMean/meanCounter}")
                meanCounter = 1
                humidityMean = 0

                humidityMean += value.toInt()

                barCounter++
            }

            itemCounter++
            if(itemCounter == humMap.size){
                xLabel.add("$hourFrom:00/now")
                Log.w("media bar", "$humidityMean $meanCounter ${humidityMean/meanCounter}")
                var mean = humidityMean/meanCounter
                ourBarEntries.add(BarEntry(barCounter.toFloat(), mean.toFloat()))
            }
        }*/



        /*for ((key, value) in humMap) {
            *//*ourBarEntries.add(BarEntry(counter.toFloat(), value))
            xLabel.add(key)
            counter++*//*
            date = java.util.Date(key.toLong() * 1000)
            var date2 = java.util.Date((key.toLong() + (30*60)) * 1000)
            Log.w("piu un ora",date.toString() + "  " +date2.toString() )
            timestampHour = sdfhour.format(date).toString()

            if (actualHour == ""){
                actualHour = timestampHour
            }

            if (timestampHour != actualHour){
                xLabel.add("$actualHour:00/$timestampHour:00")
                actualHour = timestampHour
                ourBarEntries.add(BarEntry(barCounter.toFloat(), (humidityMean/meanCounter).toFloat()))
                meanCounter = 1
                humidityMean = 0
                barCounter++
            }

            humidityMean += value.toInt()
            meanCounter++
            Log.w("media", "$humidityMean $meanCounter")


            itemCounter++
            if(itemCounter == humMap.size){
                xLabel.add("$actualHour:00/now")
                ourBarEntries.add(BarEntry(barCounter.toFloat(), (humidityMean/meanCounter).toFloat()))
            }

        }*/


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
        barChart.setFitBars(false);
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

    override fun onPause() {
        super.onPause()
        potRef.addListenerForSingleValueEvent(humidityListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        potRef.addListenerForSingleValueEvent(humidityListener)
    }
}