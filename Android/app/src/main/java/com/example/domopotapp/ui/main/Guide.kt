package com.example.domopotapp.ui.main

import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.domopotapp.R
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream


class Guide : Fragment(R.layout.guide_fragment) {
    companion object {
        fun newInstance() = Guide()
        fun newInstanceWithBundle(b: Bundle): Guide{
            val f = Guide()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()

    // TODO prendere i dati da Firebase
    val l: List<PlantTypeData> = listOf(
        PlantTypeData("Peperomia", "plant_img/peperomia.png", 3),
        PlantTypeData("Filodendro", "plant_img/filodendro.png", 5),
        PlantTypeData("Bonsai", "plant_img/bonsai.png", 9),
        PlantTypeData("Peperomia", "plant_img/peperomia.png", 3),
        PlantTypeData("Filodendro", "plant_img/filodendro.png", 5),
        PlantTypeData("Bonsai", "plant_img/bonsai.png", 9),
        PlantTypeData("Peperomia", "plant_img/peperomia.png", 3),
        PlantTypeData("Filodendro", "plant_img/filodendro.png", 5),
        PlantTypeData("Bonsai", "plant_img/bonsai.png", 9),
        PlantTypeData("Peperomia", "plant_img/peperomia.png", 3),
        PlantTypeData("Filodendro", "plant_img/filodendro.png", 5),
        PlantTypeData("Bonsai", "plant_img/bonsai.png", 9),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv: RecyclerView = view.findViewById(R.id.plantTypesRV)
        rv.layoutManager = LinearLayoutManager(activity)
        rv.adapter = PlantTypeAdapter(l)
    }
}

data class PlantTypeData(val name: String, val image: String, val difficulty: Int)

class PlantTypeAdapter(private val l: List<PlantTypeData>): RecyclerView.Adapter<PlantTypeAdapter.PlantTypeViewHolder>() {
    class PlantTypeViewHolder(v: View): RecyclerView.ViewHolder(v) {
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
                color = ColorStateList.valueOf(holder.ptDifficultyBar.context.getColor(R.color.primary))
                difficultyText = holder.ptDifficultyText.context.getString(R.string.difficulty_easy)
            }
            l[position].difficulty <= 7 -> {
                color = ColorStateList.valueOf(holder.ptDifficultyBar.context.getColor(R.color.warning))
                difficultyText = holder.ptDifficultyText.context.getString(R.string.difficulty_medium)
            }
            else -> {
                color = ColorStateList.valueOf(holder.ptDifficultyBar.context.getColor(R.color.danger))
                difficultyText = holder.ptDifficultyText.context.getString(R.string.difficulty_hard)
            }
        }

        holder.ptName.text = l[position].name
        holder.ptDifficulty.text = l[position].difficulty.toString()
        holder.ptDifficultyBar.progress = l[position].difficulty

        holder.ptDifficulty.setTextColor(color)
        holder.ptDifficultyBar.progressTintList = color
        holder.ptDifficultyText.text = difficultyText

        val assetManager: AssetManager = holder.ptImage.context.assets
        val ims: InputStream = assetManager.open(l[position].image)
        val d = Drawable.createFromStream(ims, null)
        holder.ptImage.setImageDrawable(d)
    }

    override fun getItemCount(): Int {
        return l.size
    }
}