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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.domopotapp.*


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

        val title: TextView = view.findViewById(R.id.guideTitle)
        val rv: RecyclerView = view.findViewById(R.id.plantTypesRV)

        if (viewModel.choosePTModeOn) title.text = "Scegli specie"

        rv.layoutManager = LinearLayoutManager(activity)
        rv.adapter = PlantTypeAdapter(viewModel.plantTypes.values.toList(), viewModel, this)
    }
}

class PlantTypeAdapter(private val l: List<PlantTypeData>, private val viewModel: MainViewModel, private val fragment: Fragment) :
    RecyclerView.Adapter<PlantTypeAdapter.PlantTypeViewHolder>() {
    class PlantTypeViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ptName: TextView = v.findViewById(R.id.plantTypeName)
        val ptDifficulty: TextView = v.findViewById(R.id.plantTypeDifficulty)
        val ptDifficultyText: TextView = v.findViewById(R.id.difficultyText)
        val ptDifficultyBar: ProgressBar = v.findViewById(R.id.difficultyBar)
        val ptImage: ImageView = v.findViewById(R.id.plantTypeImage)
        val ptCard: CardView = v.findViewById(R.id.ptCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantTypeViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.plant_type_item, parent, false)
        return PlantTypeViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PlantTypeViewHolder, position: Int) {
        val color: ColorStateList = getDifficultyColor(l[position].difficulty, holder.ptName.context)
        val difficultyText: String = getDifficultyText(l[position].difficulty, holder.ptName.context)

        holder.ptName.text = l[position].name
        holder.ptDifficulty.text = l[position].difficulty.toString()
        holder.ptDifficultyBar.progress = l[position].difficulty

        holder.ptDifficulty.setTextColor(color)
        holder.ptDifficultyBar.progressTintList = color
        holder.ptDifficultyText.text = difficultyText

        linkAssetImage(holder.ptImage, l[position].img)

        if (viewModel.choosePTModeOn) {
            holder.ptCard.setOnClickListener {
                viewModel.currentPlantType = l[position].id
                viewModel.choosePTModeOn = false
                findNavController(fragment).navigate(viewModel.choosePTAction)
            }
        }
        else {
            holder.ptCard.setOnClickListener {
                viewModel.currentPlantType = l[position].id
                findNavController(fragment).navigate(R.id.guide_to_plantTypeNav)
            }
        }
    }

    override fun getItemCount(): Int {
        return l.size
    }
}