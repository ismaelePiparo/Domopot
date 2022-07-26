package com.example.domopotapp

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.ViewPager2
import com.example.domopotapp.ui.main.Details
import com.example.domopotapp.ui.main.MainViewModel
import com.google.firebase.database.DatabaseReference
import java.io.InputStream
import java.time.*

val defaultFirebaseOnFailureListener =
    { it: Exception -> Log.e("firebase", "Error getting data", it) }

fun linkAssetImage(imageView: ImageView, fileName: String) {
    val assetManager: AssetManager = imageView.context.assets
    val ims: InputStream = assetManager.open(fileName)
    val d = Drawable.createFromStream(ims, null)
    imageView.setImageDrawable(d)
}

fun setSupportsChangeAnimations(viewPager: ViewPager2, enable: Boolean) {
    for (i in 0 until viewPager.childCount) {
        val view = viewPager.getChildAt(i)
        if (view is RecyclerView) {
            val animator = view.itemAnimator
            if (animator != null) {
                (animator as SimpleItemAnimator).supportsChangeAnimations = enable
            }
            break
        }
    }
}

fun getLastWateringFromTimestamp(timestamp: Long): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault())
}

fun getTimeDistanceString(d: LocalDateTime): String {
    val i = Duration.between(d.minusHours(2), LocalDateTime.now())
    return when {
        i.toDays() > 0 -> {
            "${i.toDays()}g"
        }
        i.toHours() > 0 -> {
            "${i.toHours()}h"
        }
        i.toMinutes() > 0 -> {
            "${i.toMinutes()}m"
        }
        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                "${i.toSeconds()}s"
            } else {
                "${i.toMinutes()}m"
            }
        }
    }
}

fun getConnectionStatusFromTimestamp(timestamp: Int): Boolean {
    //TODO test if it works...
    //Controlla se online status è maggiore del tempo corrente - 5 minuti
    // onlineStatus si aggiorna da ESP ogni 10 secondi
    //Log.w("Current ts -5 min", ((System.currentTimeMillis() / 1000)-250).toString())
    return timestamp > (System.currentTimeMillis() / 1000) - 500
}

fun getTwoDigitsNumber(num: Int): String {
    return if (num < 10) "0$num"
    else "$num"
}

fun getHMTimeString(hour: Int, minutes: Int): String {
    val hString = getTwoDigitsNumber(hour)
    val minString = getTwoDigitsNumber(minutes)
    return "$hString:$minString"
}

@RequiresApi(Build.VERSION_CODES.O)
fun getTimestampFromTimeString(timeString: String): Long {
    val now = LocalDate.now()
    val month =
        if (now.monthValue < 10) "0${now.monthValue}"
        else "${now.monthValue}"
    return Instant.parse("${now.year}-$month-${now.dayOfMonth}T$timeString:00.000Z")
        .toEpochMilli() / 1000
}

@RequiresApi(Build.VERSION_CODES.O)
fun getTimeStringFromTimestamp(timestamp: Long): String {
    var d = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault())
    d = d.minusHours(2)
    return getHMTimeString(d.hour, d.minute)
}

@SuppressLint("UseCompatLoadingForDrawables")
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
            context.theme
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
    val context = inputText.context
    if (checkboxIsChecked) {
        inputText.isEnabled = true
        inputText.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.info, context.theme))
        inputText.setHintTextColor(resources.getColor(R.color.info, context.theme))
        label.setTextColor(resources.getColor(R.color.info, context.theme))
    } else {
        inputText.isEnabled = false
        inputText.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.light, context.theme))
        inputText.setHintTextColor(resources.getColor(R.color.light, context.theme))
        label.setTextColor(resources.getColor(R.color.light, context.theme))
        inputText.setText("")
    }
}

fun updateInputTextFocus(inputText: EditText, label: TextView, isFocused: Boolean) {
    val resources = inputText.resources
    val context = inputText.context

    if (isFocused) {
        inputText.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.primary, context.theme))
        label.setTextColor(resources.getColor(R.color.primary, context.theme))
    } else {
        inputText.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.info, context.theme))
        label.setTextColor(resources.getColor(R.color.info, context.theme))
    }
}

fun setEditPlantType(
    vm: MainViewModel,
    editPlantLayout: ConstraintLayout,
    cardSelectedType: TextView,
    cardChooseTypeButton: Button? = null,
    cardEditButton: ImageButton? = null
) {
    if (vm.currentPlantType.isNotEmpty()) {
        cardSelectedType.text = vm.plantTypes[vm.currentPlantType]!!.name

        cardChooseTypeButton?.visibility = View.INVISIBLE
        cardSelectedType.visibility = View.VISIBLE
        cardEditButton?.visibility = View.VISIBLE
        editPlantLayout.visibility = View.VISIBLE
    } else if (vm.currentPot.isNotEmpty() && !vm.userPots[vm.currentPot]?.type.isNullOrEmpty()) {
        // TODO testare
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
        viewModel.currentPlantType = viewModel.userPots[viewModel.currentPot]!!.type!!
        editPlantLayout.visibility = View.VISIBLE
    }

    cardBackButton.setOnClickListener {
        editPlantLayout.visibility = View.GONE
        viewModel.currentPlantType = ""
    }

    cardNameInput.setOnFocusChangeListener { _, isFocused ->
        updateInputTextFocus(cardNameInput, cardNameLabel, isFocused)
    }

    cardCheckBox.setOnCheckedChangeListener { _, isChecked ->
        updateCheckBoxEditText(cardNameInput, cardNameLabel, isChecked)

        if (!isChecked) viewModel.editPlantSelectedName = ""
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

@SuppressLint("SetTextI18n")
@RequiresApi(Build.VERSION_CODES.M)
fun bindMyPlantsView(
    pd: PotData,
    viewModel: MainViewModel,
    plantName: TextView,
    plantType: TextView,
    plantImage: ImageView,
    manualWateringButton: ImageButton,
    sureCardLayout: ConstraintLayout,
    sureCardTitleBackButton: ImageButton,
    sureCardConfirm: Button,
    connectionStatusIcon: ImageView,
    modeIcon: ImageView,
    humidity: TextView,
    temperature: TextView,
    waterLevel: TextView,
) {
    if (pd.name == "") {
        plantName.text = viewModel.plantTypes[pd.type]!!.name
        plantType.text = ""
    } else {
        plantName.text = pd.name
        plantType.text = viewModel.plantTypes[pd.type]!!.name
    }

    if (pd.manualMode) {
        if (pd.commandMode == "Immediate") manualWateringButton.visibility = View.VISIBLE
        else manualWateringButton.visibility = View.GONE

        manualWateringButton.setOnClickListener {
            sureCardLayout.visibility = View.VISIBLE
        }
        sureCardTitleBackButton.setOnClickListener {
            sureCardLayout.visibility = View.GONE
        }
        sureCardConfirm.setOnClickListener {
            Log.w("Annaffia", "Annaffio: ${viewModel.currentPot}")
            viewModel.db.child("Pots/" + viewModel.currentPot + "/Commands/Immediate/Annaffia")
                .setValue(true)
            sureCardLayout.visibility = View.GONE
        }
    } else {
        manualWateringButton.visibility = View.GONE
    }

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

    linkAssetImage(plantImage, pd.image)
}