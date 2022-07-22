package com.example.domopotapp.ui.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.R
import com.example.domopotapp.updateCheckBoxEditText
import com.example.domopotapp.updateInputTextFocus

class ConfigCompleted : Fragment(R.layout.config_completed_fragment) {
    companion object {
        fun newInstance() = ConfigCompleted()

        fun newInstanceWithBundle(b: Bundle): ConfigCompleted {
            val f = ConfigCompleted()
            f.arguments = b
            return f
        }
    }

    val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userPotsRef = viewModel.mAuth.currentUser!!.let {
            viewModel.db.child("Users")
                .child(it.uid)
                .child("pots")
        }
        val globalPotsRef = viewModel.db.child("Pots")

        val createPlantButton: Button = view.findViewById(R.id.configCompletedCreatePlantButton)

        val createPlantCard: ConstraintLayout = view.findViewById(R.id.createPlantCardLayout)
        val cardBackButton: ImageButton = view.findViewById(R.id.createPlantCardTitleBackButton)
        val cardChooseTypeButton: Button = view.findViewById(R.id.createPlantCardChooseType)
        val cardSelectedType: TextView = view.findViewById(R.id.createPlantCardType)
        val cardEditButton: ImageButton = view.findViewById(R.id.createPlantCardEditPlantType)
        val cardCheckBox: CheckBox = view.findViewById(R.id.createPlantCardCheckBox)
        val cardNameLabel: TextView = view.findViewById(R.id.createPlantCardNameLabel)
        val cardNameInput: EditText = view.findViewById(R.id.createPlantCardNameInput)
        val cardConfirmButton: Button = view.findViewById(R.id.createPlantCardConfirm)

        if (!viewModel.currentPlantType.isEmpty()) {
            cardSelectedType.text = viewModel.plantTypes[viewModel.currentPlantType]!!.name

            createPlantCard.visibility = View.VISIBLE
            cardChooseTypeButton.visibility = View.INVISIBLE
            cardSelectedType.visibility = View.VISIBLE
            cardEditButton.visibility = View.VISIBLE
        }

        if (!viewModel.createPlantSelectedName.isEmpty()) {
            cardNameInput.setText(viewModel.createPlantSelectedName)
            cardCheckBox.isChecked = true
            updateCheckBoxEditText(cardNameInput, cardNameLabel, true)
        }

        createPlantButton.setOnClickListener {
            createPlantCard.visibility = View.VISIBLE
        }

        cardBackButton.setOnClickListener {
            createPlantCard.visibility = View.GONE
        }

        cardNameInput.setOnFocusChangeListener { _, isFocused ->
            updateInputTextFocus(cardNameInput, cardNameLabel, isFocused)
        }

        cardCheckBox.setOnCheckedChangeListener { _, isChecked ->
            updateCheckBoxEditText(cardNameInput, cardNameLabel, isChecked)
        }

        val choosePlantListener: (View) -> Unit = {
            viewModel.createPlantSelectedName = cardNameInput.text.toString()
            viewModel.choosePTModeOn = true
            findNavController().navigate(R.id.action_configCompleted_to_guide)
        }

        cardChooseTypeButton.setOnClickListener(choosePlantListener)
        cardEditButton.setOnClickListener(choosePlantListener)

        // TODO aggiungere onfailurelistener
        cardConfirmButton.setOnClickListener {
            if (viewModel.currentPlantType.isEmpty()) {
                // TODO comunicare che si deve selezionare qualcosa
            }
            else {
                globalPotsRef.child(viewModel.currentPot).child("PlantType")
                    .setValue(viewModel.currentPlantType).addOnSuccessListener {
                    if (!cardNameInput.text.toString().isEmpty()) {
                        userPotsRef.child(viewModel.currentPot)
                            .setValue(cardNameInput.text.toString()).addOnSuccessListener {
                                findNavController().navigate(R.id.action_configCompleted_to_home2)
                            }
                    } else findNavController().navigate(R.id.action_configCompleted_to_home2)
                }
            }
        }
    }
}