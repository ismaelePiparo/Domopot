package com.example.domopotapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.domopotapp.PotData
import com.example.domopotapp.R
import com.example.domopotapp.bindMyPlantsView
import com.example.domopotapp.getTimeDistanceString


class PlantOverviewAdapter(var l: MutableList<PotData>, val viewModel: MainViewModel) :
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
        val sureCardLayout: ConstraintLayout = v.findViewById(R.id.plantOVSureCardLayout)
        val sureCardTitleBackButton: ImageButton = v.findViewById(R.id.plantOVSureCardTitleBackButton)
        val sureCardConfirm: Button = v.findViewById(R.id.plantOVSureCardConfirm)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantOverviewViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.plant_overview_item, parent, false)
        return PlantOverviewViewHolder(v)
    }

    override fun onBindViewHolder(holder: PlantOverviewViewHolder, position: Int) {
        holder.lastWatering.text = getTimeDistanceString(l[position].lastWatering)
        bindMyPlantsView(
            l[position],
            viewModel,
            holder.plantName,
            holder.plantType,
            holder.plantImage,
            holder.manualWateringButton,
            holder.sureCardLayout,
            holder.sureCardTitleBackButton,
            holder.sureCardConfirm,
            holder.connectionStatusIcon,
            holder.modeIcon,
            holder.humidity,
            holder.temperature,
            holder.waterLevel,
        )
    }

    override fun getItemCount(): Int {
        return l.size
    }

    fun submitList(newL: MutableList<PotData>) {
        val removeIndexes: MutableList<Int> = mutableListOf()

        l.forEachIndexed { i, old ->
            val index = newL.indexOf(newL.find { it.id == old.id })
            if (index == -1) removeIndexes.add(i)
        }

        newL.forEach { new ->
            val index = l.indexOf(l.find { it.id == new.id })
            if (index >= 0) updateListItem(new, index)
            else addListItem(new, l.size)
        }

        removeIndexes.sortDescending()
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