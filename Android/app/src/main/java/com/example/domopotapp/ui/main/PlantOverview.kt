package com.example.domopotapp.ui.main

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.domopotapp.R

class PlantOverviewAdapter(var l: MutableList<PotData>) :
    RecyclerView.Adapter<PlantOverviewAdapter.PlantOverviewViewHolder>() {

    class PlantOverviewViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val plantName: TextView = v.findViewById(R.id.plantName)
        val plantType: TextView = v.findViewById(R.id.plantType)
        val plantImage: ImageView = v.findViewById(R.id.plantImage)
        val humidity: TextView = v.findViewById(R.id.humidity)
        val temperature: TextView = v.findViewById(R.id.temperature)
        val waterLevel: TextView = v.findViewById(R.id.waterLevel)
        val lastWatering: TextView = v.findViewById(R.id.lastWatering)
        val manualWateringButton: ImageButton = v.findViewById(R.id.manualWateringButton)
        val connectionStatusIcon: ImageView = v.findViewById(R.id.connectionStatusIcon)
        val modeIcon: ImageView = v.findViewById(R.id.modeIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantOverviewViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.plant_overview_item, parent, false)
        return PlantOverviewViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PlantOverviewViewHolder, position: Int) {
        val pd = l[position]
        val context = holder.plantName.context

        if (pd.name == "") {
            holder.plantName.text = pd.type
            holder.plantType.text = ""
        }
        else {
            holder.plantName.text = pd.name
            holder.plantType.text = pd.type
        }

        if (pd.commandMode == "Immediate") holder.manualWateringButton.visibility = View.VISIBLE
        else holder.manualWateringButton.visibility = View.GONE

        if (pd.connectionStatus) {
            applyDrawableAndColorToIV(holder.connectionStatusIcon, R.drawable.ic_wifi, R.color.primary)
        }
        else {
            applyDrawableAndColorToIV(holder.connectionStatusIcon, R.drawable.ic_wifi_off, R.color.danger)
        }

        if (pd.manualMode) {
            applyDrawableAndColorToIV(holder.modeIcon, R.drawable.ic_face, R.color.warning)
        }
        else {
            applyDrawableAndColorToIV(holder.modeIcon, R.drawable.ic_robot, R.color.secondary)
        }

        holder.humidity.text = pd.humidity.toString() + "%"
        holder.temperature.text = pd.temperature.toString() + "°"
        holder.waterLevel.text = pd.waterLevel.toString() + "%"
        holder.lastWatering.text = pd.lastWatering.toString() + "h"

        linkAssetImage(holder.plantImage, pd.image)
    }

    override fun getItemCount(): Int {
        return l.size
    }

    fun submitList(newL: MutableList<PotData>) {
        var removeIndexes: MutableList<Int> = mutableListOf()

        l.forEachIndexed { i, old ->
            val index = newL.indexOf(newL.find { it.id == old.id })
            if (index == -1) removeIndexes.add(i)
        }

        newL.forEach { new ->
            val index = l.indexOf(l.find { it.id == new.id })
            if (index >= 0) updateListItem(new, index)
            else addListItem(new, l.size)
        }

        removeIndexes.forEach {
            removeListItem(it)
        }
    }

    private fun addListItem(newData: PotData, position: Int) {
        l.add(position, newData)
        notifyItemInserted(position)
    }

    private fun removeListItem(position: Int) {
        l.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun updateListItem(newData: PotData, position: Int) {
        l[position] = newData
        notifyItemChanged(position)
    }
}