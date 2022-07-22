package com.example.domopotapp.ui.main

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R
import com.example.domopotapp.getDifficultyColor
import com.example.domopotapp.getDifficultyText
import com.example.domopotapp.linkAssetImage

class PlantType : Fragment(R.layout.plant_type_fragment) {
    companion object {
        fun newInstance() = Guide()
        fun newInstanceWithBundle(b: Bundle): Guide{
            val f = Guide()
            f.arguments = b
            return f
        }
    }

    private val viewModel by activityViewModels<MainViewModel>()


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        val ptName: TextView = v.findViewById(R.id.guidePlantType)
        val ptDifficulty: TextView = v.findViewById(R.id.guidePtDifficulty)
        val ptDifficultyText: TextView = v.findViewById(R.id.guideDifficultyText)
        val ptDifficultyBar: ProgressBar = v.findViewById(R.id.guideDifficultyBar)
        val ptImage: ImageView = v.findViewById(R.id.guidePlantImage)
        val ptDescription: TextView = v.findViewById(R.id.guideDescription)

        val backButton: ImageButton = v.findViewById(R.id.guidebackButton)

        val pt = viewModel.plantTypes[viewModel.currentPlantType]!!
        val color: ColorStateList = getDifficultyColor(pt.difficulty, v.context)
        val difficultyText: String = getDifficultyText(pt.difficulty, v.context)

        ptName.text = pt.name
        ptDifficulty.text = pt.difficulty.toString()
        ptDifficultyBar.progress = pt.difficulty
        ptDescription.text = pt.description

        ptDifficulty.setTextColor(color)
        ptDifficultyBar.progressTintList = color
        ptDifficultyText.text = difficultyText

        linkAssetImage(ptImage, pt.img)

        backButton.setOnClickListener{
            viewModel.currentPlantType = ""
            findNavController().navigate(R.id.plantTypeNav_to_guide)
        }
    }
}