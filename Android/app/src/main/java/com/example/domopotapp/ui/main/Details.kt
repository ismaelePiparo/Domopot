package com.example.domopotapp.ui.main

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.domopotapp.*
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import java.time.format.DateTimeFormatter


class Details : Fragment(R.layout.details_fragment) {
    companion object {
        fun newInstance() = Guide()
        fun newInstanceWithBundle(b: Bundle): Guide {
            val f = Guide()
            f.arguments = b
            return f
        }
    }

    private val vm by activityViewModels<MainViewModel>()

    private lateinit var userPotsRef: DatabaseReference
    private lateinit var globalPotsRef: DatabaseReference
    private lateinit var singlePotListener: ChildEventListener

    private lateinit var detailsPlantName: TextView
    private lateinit var detailsPlantType: TextView
    private lateinit var detailsPlantImage: ImageView
    private lateinit var detailsManualWateringButton: ImageButton
    private lateinit var detailsModeIcon: ImageView
    private lateinit var detailsTimePickerLayout: ConstraintLayout
    private lateinit var detailsTimePicker: TimePicker

    private lateinit var detailsConnectionStatusIcon: ImageView
    private lateinit var detailsCardLastWateringDate: TextView
    private lateinit var detailsCardHumidityValue: TextView
    private lateinit var detailsCardWaterLevelValue: TextView
    private lateinit var detailsCardTemperatureValue: TextView
    private lateinit var detailsCardHumidityBar: ProgressBar
    private lateinit var detailsCardWaterLevelBar: ProgressBar
    private lateinit var detailsTitleBackButton: ImageButton
    private lateinit var detailsEditPlantButton: ImageButton
    private lateinit var graphBtn: Button

    private lateinit var detailsCardWateringModeLayout: ConstraintLayout
    private lateinit var detailsCardWateringModeTitle: TextView
    private lateinit var detailsCardRadioGroup: RadioGroup
    private lateinit var detailsCardRadioButtonHumidity: RadioButton
    private lateinit var detailsCardRadioButtonProgram: RadioButton
    private lateinit var detailsCardRadioButtonImmediate: RadioButton
    private lateinit var detailsCardWateringModeSubtitle: TextView
    private lateinit var detailsCardWateringModeSeekBar: SeekBar
    private lateinit var detailsCardWateringModeSeekBarLabel: TextView
    private lateinit var detailsCardWateringModeDescription: TextView
    private lateinit var detailsCardWateringModeSeekBarTitle: TextView
    private lateinit var detailsCardWateringModeTimePickerTitle: TextView
    private lateinit var detailsCardWateringModeTimePickerButton: Button

    private lateinit var detailsCardModeSwitch: Switch
    private lateinit var detailsCardTemperatureBar: ProgressBar
    private lateinit var detailsEditCardLayout: ConstraintLayout
    private lateinit var detailsEditCardType: TextView
    private lateinit var detailsEditCardNameLabel: TextView
    private lateinit var detailsEditCardCheckBox: CheckBox
    private lateinit var detailsEditCardNameInput: EditText
    private lateinit var detailsEditCardTitleBackButton: ImageButton
    private lateinit var detailsEditCardEditPlantType: ImageButton
    private lateinit var detailsEditCardConfirm: Button

    private lateinit var detailsSureCardLayout: ConstraintLayout
    private lateinit var detailsSureCardTitleBackButton: ImageButton
    private lateinit var detailsSureCardConfirm: Button

    // TODO tasto elimina

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPotsRef = vm.mAuth.currentUser!!.let {
            vm.db.child("Users")
                .child(it.uid)
                .child("pots")
        }
        globalPotsRef = vm.db.child("Pots")

        singlePotListener = SinglePotListener(vm, this)
        globalPotsRef.addChildEventListener(singlePotListener)

        linkIds(view)

        vm.choosePTAction = R.id.action_guide_to_details

        detailsTimePicker.setIs24HourView(true)

        detailsCardModeSwitch.setOnCheckedChangeListener { _, isOn ->
            if (isOn) {
                detailsCardModeSwitch.text = "Automatica"
                detailsCardModeSwitch.thumbTintList =
                    getColorStateListFromId(R.color.secondary, context)
                detailsCardModeSwitch.trackTintList =
                    getColorStateListFromId(R.color.info, context)

                detailsCardWateringModeLayout.visibility = View.GONE
            } else {
                detailsCardModeSwitch.text = "Manuale"
                detailsCardModeSwitch.thumbTintList =
                    getColorStateListFromId(R.color.warning, context)

                detailsCardWateringModeLayout.visibility = View.VISIBLE
            }
        }
        detailsCardModeSwitch.setOnClickListener {
            val isOn = detailsCardModeSwitch.isChecked
            if (isOn) vm.uploadCurrentPot(humidityThreshold = vm.plantTypes[vm.userPots[vm.currentPot]!!.type]!!.humidityThreshold)
            vm.uploadCurrentPot(manualMode = !isOn)
            updateView()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Convertire a smoothscroll se si riesce
                view.findViewById<ScrollView>(R.id.detailsScrollView)
                    .scrollToDescendant(detailsCardWateringModeSeekBar)
            }
        }

        graphBtn.setOnClickListener {
            findNavController().navigate(R.id.details_to_graph)
        }
        detailsTitleBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_details_to_home2)
        }

        detailsCardRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            checkRadioButton(R.id.detailsCardRadioButtonHumidity, checkedId, view)
            checkRadioButton(R.id.detailsCardRadioButtonProgram, checkedId, view)
            checkRadioButton(R.id.detailsCardRadioButtonImmediate, checkedId, view)

            updateSeekBarLabel(detailsCardWateringModeSeekBar.progress)
        }
        detailsCardRadioButtonHumidity.setOnClickListener {
            vm.uploadCurrentPot(commandMode = "Humidity")
        }
        detailsCardRadioButtonProgram.setOnClickListener {
            vm.uploadCurrentPot(commandMode = "Program")
        }
        detailsCardRadioButtonImmediate.setOnClickListener {
            vm.uploadCurrentPot(commandMode = "Immediate")
        }

        detailsCardWateringModeSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                updateSeekBarLabel(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (vm.userPots[vm.currentPot]!!.commandMode == "Humidity") vm.uploadCurrentPot(
                    humidityThreshold = detailsCardWateringModeSeekBar.progress
                )
                else vm.uploadCurrentPot(waterQuantity = detailsCardWateringModeSeekBar.progress)
            }
        })

        detailsTimePickerLayout.setOnClickListener {
            detailsTimePickerLayout.visibility = View.GONE
        }
        detailsCardWateringModeTimePickerButton.setOnClickListener {
            detailsTimePickerLayout.visibility = View.VISIBLE
        }
        detailsTimePicker.setOnTimeChangedListener { _, hour, minutes ->
            val timeString = getHMTimeString(hour, minutes)
            detailsCardWateringModeTimePickerButton.text = timeString
            vm.uploadCurrentPot(programTiming = getTimestampFromTimeString(timeString))
        }

        setEditPlantListeners(
            false,
            this,
            R.id.action_details_to_guide,
            vm,
            userPotsRef,
            globalPotsRef,
            detailsEditCardLayout,
            detailsEditCardTitleBackButton,
            detailsEditCardNameInput,
            detailsEditCardNameLabel,
            detailsEditCardCheckBox,
            detailsEditCardEditPlantType,
            detailsEditCardConfirm,
            null,
            detailsEditPlantButton
        )

        updateView()
    }

    override fun onDestroy() {
        super.onDestroy()
        globalPotsRef.removeEventListener(singlePotListener)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun updateView() {
        if (vm.editPlantSelectedName.isEmpty()) {
            vm.editPlantSelectedName = vm.userPots[vm.currentPot]!!.name
        }

        if (vm.userPots[vm.currentPot]!!.manualMode) detailsCardWateringModeLayout.visibility =
            View.VISIBLE
        else detailsCardWateringModeLayout.visibility = View.GONE

        detailsCardLastWateringDate.text = vm.userPots[vm.currentPot]!!.lastWatering.minusHours(2).format(DateTimeFormatter.ofPattern("d MMM HH:mm"))

        detailsCardHumidityBar.progress = vm.userPots[vm.currentPot]!!.humidity
        detailsCardWaterLevelBar.progress = vm.userPots[vm.currentPot]!!.waterLevel
        detailsCardTemperatureBar.progress = vm.userPots[vm.currentPot]!!.temperature

        bindMyPlantsView(
            vm.userPots[vm.currentPot]!!,
            vm,
            detailsPlantName,
            detailsPlantType,
            detailsPlantImage,
            detailsManualWateringButton,
            detailsSureCardLayout,
            detailsSureCardTitleBackButton,
            detailsSureCardConfirm,
            detailsConnectionStatusIcon,
            detailsModeIcon,
            detailsCardHumidityValue,
            detailsCardTemperatureValue,
            detailsCardWaterLevelValue,
        )

        setEditPlantType(
            vm,
            detailsEditCardLayout,
            detailsEditCardType
        )
        setEditPlantName(
            vm,
            detailsEditCardNameInput,
            detailsEditCardCheckBox,
            detailsEditCardNameLabel
        )

        detailsCardModeSwitch.isChecked = !vm.userPots[vm.currentPot]!!.manualMode
        detailsCardModeSwitch.trackTintList = getColorStateListFromId(R.color.info, context)

        when (vm.userPots[vm.currentPot]!!.commandMode) {
            "Humidity" -> detailsCardRadioGroup.check(R.id.detailsCardRadioButtonHumidity)
            "Program" -> detailsCardRadioGroup.check(R.id.detailsCardRadioButtonProgram)
            else -> detailsCardRadioGroup.check(R.id.detailsCardRadioButtonImmediate)
        }

        detailsCardWateringModeSeekBar.progress =
            if (vm.userPots[vm.currentPot]!!.commandMode != "Humidity") vm.userPots[vm.currentPot]!!.waterQuantity
            else vm.userPots[vm.currentPot]!!.humidityThreshold
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkRadioButton(currentId: Int, checkedId: Int, view: View) {
        when (checkedId) {
            detailsCardRadioButtonHumidity.id -> {
                detailsCardWateringModeSubtitle.text = "Umidità terreno"
                detailsCardWateringModeDescription.text =
                    "Imposta un livello di umidità del terreno che sarà mantenuto dal sistema."
                detailsCardWateringModeSeekBarTitle.text = "Livello di umidità"
                detailsCardWateringModeTimePickerTitle.visibility = View.GONE
                detailsCardWateringModeTimePickerButton.visibility = View.GONE
            }
            detailsCardRadioButtonProgram.id -> {
                detailsCardWateringModeSubtitle.text = "Tempo"
                detailsCardWateringModeDescription.text =
                    "Il sistema innaffierà una volta al giorno all'ora prefissata. Usa lo slider per scegliere la quantità d'acqua usata."
                detailsCardWateringModeSeekBarTitle.text = "Quantità d'acqua"
                detailsCardWateringModeTimePickerButton.text = getTimeStringFromTimestamp(vm.userPots[vm.currentPot]!!.programTiming)
                detailsCardWateringModeTimePickerTitle.visibility = View.VISIBLE
                detailsCardWateringModeTimePickerButton.visibility = View.VISIBLE
            }
            else -> {
                detailsCardWateringModeSubtitle.text = "Manuale"
                detailsCardWateringModeDescription.text =
                    "Innaffia manualmente nei momenti che preferisci usando il pulsante apposito. Usa lo slider per scegliere la quantità d'acqua usata."
                detailsCardWateringModeSeekBarTitle.text = "Quantità d'acqua"
                detailsCardWateringModeTimePickerTitle.visibility = View.GONE
                detailsCardWateringModeTimePickerButton.visibility = View.GONE
            }
        }

        if (currentId == checkedId) {
            view.findViewById<RadioButton>(currentId).buttonTintList =
                getColorStateListFromId(R.color.primary, context)
        } else {
            view.findViewById<RadioButton>(currentId).buttonTintList =
                getColorStateListFromId(R.color.light, context)
        }
    }

    fun updateSeekBarLabel(progress: Int) {
        val end =
            if (vm.userPots[vm.currentPot]!!.commandMode == "Humidity") "%"
            else "ml"

        detailsCardWateringModeSeekBarLabel.text = progress.toString() + end
    }

    fun linkIds(view: View) {
        detailsPlantName = view.findViewById(R.id.detailsPlantName)
        detailsPlantType = view.findViewById(R.id.detailsPlantType)
        detailsPlantImage = view.findViewById(R.id.detailsPlantImage)
        detailsManualWateringButton = view.findViewById(R.id.detailsManualWateringButton)
        detailsModeIcon = view.findViewById(R.id.detailsModeIcon)
        detailsConnectionStatusIcon = view.findViewById(R.id.detailsConnectionStatusIcon)
        detailsTitleBackButton = view.findViewById(R.id.detailsTitleBackButton)
        detailsEditPlantButton = view.findViewById(R.id.detailsEditPlantButton)
        graphBtn = view.findViewById(R.id.detailsGraph)
        detailsTimePickerLayout = view.findViewById(R.id.detailsTimePickerLayout)
        detailsTimePicker = view.findViewById(R.id.detailsTimePicker)

        detailsCardWateringModeLayout = view.findViewById(R.id.detailsCardWateringModeLayout)
        detailsCardWateringModeTitle = view.findViewById(R.id.detailsCardWateringModeTitle)
        detailsCardRadioGroup = view.findViewById(R.id.detailsCardRadioGroup)
        detailsCardRadioButtonHumidity = view.findViewById(R.id.detailsCardRadioButtonHumidity)
        detailsCardRadioButtonProgram = view.findViewById(R.id.detailsCardRadioButtonProgram)
        detailsCardRadioButtonImmediate = view.findViewById(R.id.detailsCardRadioButtonImmediate)
        detailsCardWateringModeSubtitle = view.findViewById(R.id.detailsCardWateringModeSubtitle)
        detailsCardWateringModeSeekBar = view.findViewById(R.id.detailsCardWateringModeSeekBar)
        detailsCardWateringModeSeekBarLabel =
            view.findViewById(R.id.detailsCardWateringModeSeekBarLabel)
        detailsCardWateringModeDescription =
            view.findViewById(R.id.detailsCardWateringModeDescription)
        detailsCardWateringModeSeekBarTitle =
            view.findViewById(R.id.detailsCardWateringModeSeekBarTitle)
        detailsCardWateringModeTimePickerTitle =
            view.findViewById(R.id.detailsCardWateringModeTimePickerTitle)
        detailsCardWateringModeTimePickerButton =
            view.findViewById(R.id.detailsCardWateringModeTimePickerButton)

        detailsCardModeSwitch = view.findViewById(R.id.detailsCardModeSwitch)
        detailsCardLastWateringDate = view.findViewById(R.id.detailsCardLastWateringDate)
        detailsCardHumidityValue = view.findViewById(R.id.detailsCardHumidityValue)
        detailsCardWaterLevelValue = view.findViewById(R.id.detailsCardWaterLevelValue)
        detailsCardTemperatureValue = view.findViewById(R.id.detailsCardTemperatureValue)
        detailsCardHumidityBar = view.findViewById(R.id.detailsCardHumidityBar)
        detailsCardWaterLevelBar = view.findViewById(R.id.detailsCardWaterLevelBar)
        detailsCardTemperatureBar = view.findViewById(R.id.detailsCardTemperatureBar)

        detailsEditCardLayout = view.findViewById(R.id.detailsEditCardLayout)
        detailsEditCardType = view.findViewById(R.id.detailsEditCardType)
        detailsEditCardNameLabel = view.findViewById(R.id.detailsEditCardNameLabel)
        detailsEditCardCheckBox = view.findViewById(R.id.detailsEditCardCheckBox)
        detailsEditCardNameInput = view.findViewById(R.id.detailsEditCardNameInput)
        detailsEditCardTitleBackButton = view.findViewById(R.id.detailsEditCardTitleBackButton)
        detailsEditCardEditPlantType = view.findViewById(R.id.detailsEditCardEditPlantType)
        detailsEditCardConfirm = view.findViewById(R.id.detailsEditCardConfirm)

        detailsSureCardLayout = view.findViewById(R.id.detailsSureCardLayout)
        detailsSureCardTitleBackButton = view.findViewById(R.id.detailsSureCardTitleBackButton)
        detailsSureCardConfirm = view.findViewById(R.id.detailsSureCardConfirm)
    }
}