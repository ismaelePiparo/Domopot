package com.example.domopotapp.ui.main

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.domopotapp.PotData
import com.example.domopotapp.R

class WifiSelectAdapter(private var l: MutableList<String>, private val viewModel: MainViewModel, private val wifiCard: ConstraintLayout) :
    RecyclerView.Adapter<WifiSelectAdapter.WifiSelectViewHolder>() {

    class WifiSelectViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val btn: Button = v.findViewById(R.id.wifiNetworkBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiSelectViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.wifi_select_item, parent, false)
        return WifiSelectViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: WifiSelectViewHolder, position: Int) {
        holder.btn.text = l[position]

        holder.btn.setOnClickListener {
            wifiCard.findViewById<TextView>(R.id.wifiCardTitle).text = l[position]
            viewModel.currentWifiSelection = l[position]
            wifiCard.visibility = View.VISIBLE
            //wifiCard.findViewById<TextView>(R.id.wifiCardPasswordInput).requestFocus()
        }
    }

    override fun getItemCount(): Int {
        return l.size
    }

    fun submitList(newL: MutableList<String>, loadingIcon: ImageView) {
        if (newL.size > 0 && loadingIcon.isVisible) loadingIcon.visibility = View.GONE
        else if (newL.size == 0 && !loadingIcon.isVisible) loadingIcon.visibility = View.VISIBLE

        var removeIndexes: MutableList<Int> = mutableListOf()

        l.forEachIndexed { i, old ->
            val index = newL.indexOf(newL.find { it == old })
            if (index == -1) removeIndexes.add(i)
        }

        newL.forEach { new ->
            val index = l.indexOf(l.find { it == new })
            if (index >= 0) updateListItem(new, index)
            else addListItem(new, l.size)
        }

        removeIndexes.forEach {
            removeListItem(it)
        }
    }

    private fun addListItem(newData: String, position: Int) {
        l.add(position, newData)
        notifyItemInserted(position)
    }

    private fun removeListItem(position: Int) {
        l.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun updateListItem(newData: String, position: Int) {
        l[position] = newData
        notifyItemChanged(position)
    }
}