package com.example.domopotapp.ui.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.domopotapp.*

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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //disabilita il tasto back
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.w("BACK PRESSED","Attendere il completamento dell'operazione...")
        }

        val userPotsRef = viewModel.mAuth.currentUser!!.let {
            viewModel.db.child("Users")
                .child(it.uid)
                .child("pots")
        }
        val globalPotsRef = viewModel.db.child("Pots")

        val createPlantButton: Button = view.findViewById(R.id.configCompletedCreatePlantButton)

        val createPlantLayout: ConstraintLayout = view.findViewById(R.id.createPlantCardLayout)
        val cardBackButton: ImageButton = view.findViewById(R.id.createPlantCardTitleBackButton)
        val cardChooseTypeButton: Button = view.findViewById(R.id.createPlantCardChooseType)
        val cardSelectedType: TextView = view.findViewById(R.id.createPlantCardType)
        val cardEditButton: ImageButton = view.findViewById(R.id.createPlantCardEditPlantType)
        val cardCheckBox: CheckBox = view.findViewById(R.id.createPlantCardCheckBox)
        val cardNameLabel: TextView = view.findViewById(R.id.createPlantCardNameLabel)
        val cardNameInput: EditText = view.findViewById(R.id.createPlantCardNameInput)
        val cardConfirmButton: Button = view.findViewById(R.id.createPlantCardConfirm)
        val cardInfoText: TextView = view.findViewById(R.id.createPlantCardInfoText)

        viewModel.choosePTAction = R.id.action_guide_to_configCompleted

        setEditPlantType(
            viewModel,
            createPlantLayout,
            cardSelectedType,
            cardChooseTypeButton,
            cardEditButton
        )
        setEditPlantName(
            viewModel,
            cardNameInput,
            cardCheckBox,
            cardNameLabel
        )

        setEditPlantListeners(
            true,
            this,
            R.id.action_configCompleted_to_guide,
            viewModel,
            userPotsRef,
            globalPotsRef,
            createPlantLayout,
            cardBackButton,
            cardNameInput,
            cardNameLabel,
            cardCheckBox,
            cardEditButton,
            cardConfirmButton,
            createPlantButton,
            null,
            cardChooseTypeButton,
            cardInfoText
        )
    }
}