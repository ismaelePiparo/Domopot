package com.example.domopotapp.ui.main

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.domopotapp.R


data class PlantOverviewData(
    var id: String,
    var name: String,
    var image: String,
    var humidity: Int,
    var waterLevel: Int,
    var temperature: Int,
    var lastWatering: Int
)

class PlantOverviewAdapter(private var l: MutableList<PlantOverviewData>) :
    RecyclerView.Adapter<PlantOverviewAdapter.PlantOverviewViewHolder>() {

    class PlantOverviewViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val plantName: TextView = v.findViewById(R.id.plantName)
        val plantImage: ImageView = v.findViewById(R.id.plantImage)
        val humidity: TextView = v.findViewById(R.id.humidity)
        val temperature: TextView = v.findViewById(R.id.temperature)
        val waterLevel: TextView = v.findViewById(R.id.waterLevel)
        val lastWatering: TextView = v.findViewById(R.id.lastWatering)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantOverviewViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.plant_overview_item, parent, false)
        return PlantOverviewViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PlantOverviewViewHolder, position: Int) {
        holder.plantName.text = l[position].name
        holder.humidity.text = l[position].humidity.toString() + "%"
        holder.temperature.text = l[position].temperature.toString() + "°"
        holder.waterLevel.text = l[position].waterLevel.toString() + "%"
        holder.lastWatering.text = l[position].lastWatering.toString() + "h"

        linkAssetImage(holder.plantImage, l[position].image)
    }

    override fun getItemCount(): Int {
        return l.size
    }

    fun addListItem(newData: PlantOverviewData, position: Int) {
        l.add(position, newData)
        notifyItemInserted(position)
    }

    fun removeListItem(position: Int) {
        l.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateListItem(newData: PlantOverviewData, position: Int) {
        l[position].name = newData.name
        l[position].image = newData.image
        l[position].humidity = newData.humidity
        l[position].waterLevel = newData.waterLevel
        l[position].temperature = newData.temperature
        l[position].waterLevel = newData.waterLevel
        notifyItemChanged(position)
    }
}