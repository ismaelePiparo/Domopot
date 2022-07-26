package com.example.domopotapp.ui.main

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.domopotapp.*
import com.google.android.material.bottomnavigation.BottomNavigationView


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

    private lateinit var bottomNav: BottomNavigationView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNav = activity?.findViewById(R.id.bottom_navigation)!!

        val title: TextView = view.findViewById(R.id.guideTitle)
        val rv: RecyclerView = view.findViewById(R.id.plantTypesRV)
        val searchPlantTypeButton: ImageButton = view.findViewById(R.id.searchPlantTypeButton)

        val guideSearchBar: CardView = view.findViewById(R.id.guideSearchBar)
        val guideSearchBarClose: ImageButton = view.findViewById(R.id.guideSearchBarClose)
        val guideSearchBarInput: EditText = view.findViewById(R.id.guideSearchBarInput)

        if (!bottomNav.isVisible) bottomNav.visibility = View.VISIBLE

        if (viewModel.choosePTModeOn) {
            title.text = "Scegli specie"
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                Log.w("BACK PRESSED","Attendere il completamento dell'operazione...")
            }
        }

        rv.layoutManager = LinearLayoutManager(activity)
        rv.adapter = PlantTypeAdapter(viewModel.plantTypes.values.toMutableList(), viewModel, this)

        searchPlantTypeButton.setOnClickListener {
            guideSearchBar.visibility = View.VISIBLE
        }
        guideSearchBarClose.setOnClickListener {
            guideSearchBar.visibility = View.GONE
        }
        guideSearchBarInput.addTextChangedListener {
            val result = viewModel.plantTypes.filter {
                it.value.name.lowercase().contains(guideSearchBarInput.text.toString().lowercase())
            }
            (rv.adapter as PlantTypeAdapter).submitList(result.values.toMutableList())
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.menu.getItem(0).isChecked = true
    }
}

class PlantTypeAdapter(private val l: MutableList<PlantTypeData>, private val viewModel: MainViewModel, private val fragment: Fragment) :
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

    fun submitList(newL: MutableList<PlantTypeData>) {
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

    private fun addListItem(newData: PlantTypeData, position: Int) {
        l.add(position, newData)
        notifyItemInserted(position)
    }

    private fun removeListItem(position: Int) {
        l.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun updateListItem(newData: PlantTypeData, position: Int) {
        l[position] = newData
        notifyItemChanged(position)
    }
}