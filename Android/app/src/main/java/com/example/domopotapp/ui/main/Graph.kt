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
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.view.Chart
import lecho.lib.hellocharts.view.LineChartView


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

        val values: MutableList<PointValue> = ArrayList()
        values.add(PointValue(0f, 5f))
        values.add(PointValue(1f, 2f))
        values.add(PointValue(2f, 10f))


        val line: Line = Line(values).setColor(Color.RED).setCubic(true)
        val lines: MutableList<Line> = ArrayList<Line>()
        lines.add(line)

        val data = LineChartData()
        data.lines = lines

        var chart = view.findViewById<LineChartView>(R.id.chart)
        chart.lineChartData = data
    }
}