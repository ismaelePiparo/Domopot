package com.example.domopotapp

import android.content.Context
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.domopotapp.ui.main.Details
import com.example.domopotapp.ui.main.MainViewModel
import com.google.firebase.database.DatabaseReference
import java.io.InputStream
import java.util.*

val defaultFirebaseOnFailureListener =
    { it: Exception -> Log.e("firebase", "Error getting data", it) }

fun linkAssetImage(imageView: ImageView, fileName: String) {
    val assetManager: AssetManager = imageView.context.assets
    val ims: InputStream = assetManager.open(fileName)
    val d = Drawable.createFromStream(ims, null)
    imageView.setImageDrawable(d)
}

fun getLastWateringFromTimestamp(timestamp: Int): String {
    //TODO test if it works...
    //Cambiato lastWatering da Int a String
    val sdf = java.text.SimpleDateFormat("HH:mm")
    var date : Date
    date = java.util.Date(timestamp.toLong() * 1000)

    return  sdf.format(date).toString()
}

fun getConnectionStatusFromTimestamp(timestamp: Int): Boolean {
    //TODO test if it works...
    //Controlla se online status è maggiore del tempo corrente - 5 minuti
    // onlineStatus si aggiorna da ESP ogni 10 secondi
    Log.w("Current ts -5 min", ((System.currentTimeMillis() / 1000)-250).toString())
    return timestamp > (System.currentTimeMillis() / 1000)-500
}

@RequiresApi(Build.VERSION_CODES.M)
fun applyDrawableAndColorToIV(imageView: ImageView, drawableId: Int, colorId: Int) {
    val context = imageView.context
    imageView.setImageDrawable(context.resources.getDrawable(drawableId, context.theme))
    imageView.imageTintList =
        ColorStateList.valueOf(context.resources.getColor(colorId, context.theme))
}

@RequiresApi(Build.VERSION_CODES.M)
fun getDifficultyColor(difficulty: Int, context: Context): ColorStateList {
    return when {
        difficulty <= 3 -> {
            ColorStateList.valueOf(context.resources.getColor(R.color.primary, context.theme))
        }
        difficulty <= 7 -> {
            ColorStateList.valueOf(context.resources.getColor(R.color.warning, context.theme))
        }
        else -> {
            ColorStateList.valueOf(context.resources.getColor(R.color.danger, context.theme))
        }
    }
}

fun getDifficultyText(difficulty: Int, context: Context): String {
    return when {
        difficulty <= 3 -> {
            context.getString(R.string.difficulty_easy)
        }
        difficulty <= 7 -> {
            context.getString(R.string.difficulty_medium)
        }
        else -> {
            context.getString(R.string.difficulty_hard)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun getColorStateListFromId(colorId: Int, context: Context?): ColorStateList {
    return ColorStateList.valueOf(
        context!!.resources.getColor(
            colorId,
            context?.theme
        )
    )
}

fun updateHomeLayout(
    emptyUserPots: Boolean?,
    mainLayout: ConstraintLayout,
    noPlantsLayout: ConstraintLayout
) {
    when (emptyUserPots) {
        null -> {
            mainLayout.visibility = View.GONE
            noPlantsLayout.visibility = View.GONE
        }
        false -> {
            mainLayout.visibility = View.VISIBLE
            noPlantsLayout.visibility = View.GONE
        }
        else -> {
            mainLayout.visibility = View.GONE
            noPlantsLayout.visibility = View.VISIBLE
        }
    }
}

fun updateCheckBoxEditText(inputText: EditText, label: TextView, checkboxIsChecked: Boolean) {
    val resources = inputText.resources
    if (checkboxIsChecked) {
        inputText.isEnabled = true
        inputText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.info))
        inputText.setHintTextColor(resources.getColor(R.color.info))
        label.setTextColor(resources.getColor(R.color.info))
    } else {
        inputText.isEnabled = false
        inputText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.light))
        inputText.setHintTextColor(resources.getColor(R.color.light))
        label.setTextColor(resources.getColor(R.color.light))
    }
}

fun updateInputTextFocus(inputText: EditText, label: TextView, isFocused: Boolean) {
    val resources = inputText.resources
    if (isFocused) {
        inputText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
        label.setTextColor(resources.getColor(R.color.primary))
    } else {
        inputText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.info))
        label.setTextColor(resources.getColor(R.color.info))
    }
}

fun setEditPlantType(
    vm: MainViewModel,
    editPlantLayout: ConstraintLayout,
    cardSelectedType: TextView,
    cardChooseTypeButton: Button? = null,
    cardEditButton: ImageButton? = null
) {
    if (!vm.currentPlantType.isEmpty()) {
        cardSelectedType.text = vm.plantTypes[vm.currentPlantType]!!.name

        cardChooseTypeButton?.visibility = View.INVISIBLE
        cardSelectedType.visibility = View.VISIBLE
        cardEditButton?.visibility = View.VISIBLE
        editPlantLayout.visibility = View.VISIBLE
    } else if (!vm.currentPot.isEmpty()) {
        cardSelectedType.text = vm.plantTypes[vm.userPots[vm.currentPot]!!.type]!!.name
    }
}

fun setEditPlantName(
    viewModel: MainViewModel,
    cardNameInput: EditText,
    cardCheckBox: CheckBox,
    cardNameLabel: TextView
) {
    if (!viewModel.editPlantSelectedName.isEmpty()) {
        cardNameInput.setText(viewModel.editPlantSelectedName)
        cardCheckBox.isChecked = true
        updateCheckBoxEditText(cardNameInput, cardNameLabel, true)
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun setEditPlantListeners(
    createMode: Boolean,
    fragment: Fragment,
    choosePlantActionId: Int,
    viewModel: MainViewModel,
    userPotsRef: DatabaseReference,
    globalPotsRef: DatabaseReference,
    editPlantLayout: ConstraintLayout,
    cardBackButton: ImageButton,
    cardNameInput: EditText,
    cardNameLabel: TextView,
    cardCheckBox: CheckBox,
    cardEditTypeButton: ImageButton,
    cardConfirmButton: Button,
    createPlantButton: Button? = null,
    editPlantButton: ImageButton? = null,
    cardChooseTypeButton: Button? = null,
    cardInfoText: TextView? = null
) {
    createPlantButton?.setOnClickListener {
        editPlantLayout.visibility = View.VISIBLE
    }

    editPlantButton?.setOnClickListener {
        editPlantLayout.visibility = View.VISIBLE

        viewModel.currentPlantType = viewModel.userPots[viewModel.currentPot]!!.type!!
    }

    cardBackButton.setOnClickListener {
        editPlantLayout.visibility = View.GONE
    }

    cardNameInput.setOnFocusChangeListener { _, isFocused ->
        updateInputTextFocus(cardNameInput, cardNameLabel, isFocused)
    }

    cardCheckBox.setOnCheckedChangeListener { _, isChecked ->
        updateCheckBoxEditText(cardNameInput, cardNameLabel, isChecked)
    }

    val choosePlantListener: (View) -> Unit = {
        viewModel.editPlantSelectedName = cardNameInput.text.toString()
        viewModel.choosePTModeOn = true
        findNavController(fragment).navigate(choosePlantActionId)
    }

    cardChooseTypeButton?.setOnClickListener(choosePlantListener)
    cardEditTypeButton.setOnClickListener(choosePlantListener)

    // TODO aggiungere onfailurelistener
    cardConfirmButton.setOnClickListener {
        if (viewModel.currentPlantType.isEmpty()) {
            cardInfoText?.visibility = View.VISIBLE
        } else {
            globalPotsRef.child(viewModel.currentPot).child("PlantType")
                .setValue(viewModel.currentPlantType).addOnSuccessListener {
                    viewModel.currentPlantType = ""
                    userPotsRef.child(viewModel.currentPot)
                        .setValue(cardNameInput.text.toString()).addOnSuccessListener {
                            if (createMode) findNavController(fragment).navigate(R.id.action_configCompleted_to_home2)
                            else {
                                (fragment as Details).updateView()
                                editPlantLayout.visibility = View.GONE
                            }
                        }
                }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun bindMyPlantsView(
    pd: PotData,
    viewModel: MainViewModel,
    plantName: TextView,
    plantType: TextView,
    plantImage: ImageView,
    manualWateringButton: ImageButton,
    connectionStatusIcon: ImageView,
    modeIcon: ImageView,
    humidity: TextView,
    temperature: TextView,
    waterLevel: TextView,
    lastWatering: TextView,
) {
    if (pd.name == "") {
        plantName.text = viewModel.plantTypes[pd.type]!!.name
        plantType.text = ""
    } else {
        plantName.text = pd.name
        plantType.text = viewModel.plantTypes[pd.type]!!.name
    }

    Log.w("current pot",pd.id + "manual mode: "+ pd.manualMode)
    //viewModel.db.child("Pots/" + pd.id + "/AutoMode").setValue(pd.manualMode)

    if(pd.manualMode){
        Log.w("manual mode", pd.manualMode.toString())
        if (pd.commandMode == "Immediate") manualWateringButton.visibility = View.VISIBLE
        else manualWateringButton.visibility = View.GONE

        manualWateringButton.setOnClickListener {
            Log.w("Annaffia", "Annaffio: ${viewModel.currentPot}")
            viewModel.db.child("Pots/" + viewModel.currentPot + "/Commands/Immediate/Annaffia")
                .setValue(true)
        }
    }else{
        manualWateringButton.visibility = View.GONE
    }


    Log.w("commands mode", pd.commandMode)



    if (pd.connectionStatus) {
        applyDrawableAndColorToIV(
            connectionStatusIcon,
            R.drawable.ic_wifi,
            R.color.primary
        )
    } else {
        applyDrawableAndColorToIV(
            connectionStatusIcon,
            R.drawable.ic_wifi_off,
            R.color.danger
        )
    }

    if (pd.manualMode) {
        applyDrawableAndColorToIV(modeIcon, R.drawable.ic_face, R.color.warning)
    } else {
        applyDrawableAndColorToIV(modeIcon, R.drawable.ic_robot, R.color.secondary)
    }

    humidity.text = pd.humidity.toString() + "%"
    temperature.text = pd.temperature.toString() + "°"
    waterLevel.text = pd.waterLevel.toString() + "%"

    // TODO impostare lastwatering in base alla pagina
    lastWatering.text = pd.lastWatering.toString() + "h"

    linkAssetImage(plantImage, pd.image)
}