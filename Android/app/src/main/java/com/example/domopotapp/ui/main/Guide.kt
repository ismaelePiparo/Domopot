package com.example.domopotapp.ui.main

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.domopotapp.R


class Guide : Fragment(R.layout.guide_fragment) {
    companion object {
        fun newInstance() = Guide()
        fun newInstanceWithBundle(b: Bundle): Guide {
            val f = Guide()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.db.child("PlantTypes").get().addOnSuccessListener { plantTypesSnapshot ->
            plantTypesSnapshot.children.forEach {
                viewModel.plantTypes[it.key.toString()] = PlantTypeData(
                    it.child("name").value.toString(),
                    it.child("img").value.toString(),
                    (it.child("difficulty").value as Long).toInt(),
                    (it.child("humidity_threshold").value as Long).toInt(),
                    it.child("description").value.toString(),
                )
            }

            val rv: RecyclerView = view.findViewById(R.id.plantTypesRV)
            rv.layoutManager = LinearLayoutManager(activity)
            rv.adapter = PlantTypeAdapter(viewModel.plantTypes.values.toList())

        }.addOnFailureListener { defaultFirebaseOnFailureListener }
    }
}

class PlantTypeAdapter(private val l: List<PlantTypeData>) :
    RecyclerView.Adapter<PlantTypeAdapter.PlantTypeViewHolder>() {
    class PlantTypeViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ptName: TextView = v.findViewById(R.id.plantTypeName)
        val ptDifficulty: TextView = v.findViewById(R.id.plantTypeDifficulty)
        val ptDifficultyText: TextView = v.findViewById(R.id.difficultyText)
        val ptDifficultyBar: ProgressBar = v.findViewById(R.id.difficultyBar)
        val ptImage: ImageView = v.findViewById(R.id.plantTypeImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantTypeViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.plant_type_item, parent, false)
        return PlantTypeViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PlantTypeViewHolder, position: Int) {
        var color: ColorStateList
        var difficultyText: String

        when {
            l[position].difficulty <= 3 -> {
                color =
                    ColorStateList.valueOf(holder.ptDifficultyBar.context.getColor(R.color.primary))
                difficultyText = holder.ptDifficultyText.context.getString(R.string.difficulty_easy)
            }
            l[position].difficulty <= 7 -> {
                color =
                    ColorStateList.valueOf(holder.ptDifficultyBar.context.getColor(R.color.warning))
                difficultyText =
                    holder.ptDifficultyText.context.getString(R.string.difficulty_medium)
            }
            else -> {
                color =
                    ColorStateList.valueOf(holder.ptDifficultyBar.context.getColor(R.color.danger))
                difficultyText = holder.ptDifficultyText.context.getString(R.string.difficulty_hard)
            }
        }

        holder.ptName.text = l[position].name
        holder.ptDifficulty.text = l[position].difficulty.toString()
        holder.ptDifficultyBar.progress = l[position].difficulty

        holder.ptDifficulty.setTextColor(color)
        holder.ptDifficultyBar.progressTintList = color
        holder.ptDifficultyText.text = difficultyText

        linkAssetImage(holder.ptImage, l[position].img)
    }

    override fun getItemCount(): Int {
        return l.size
    }
}