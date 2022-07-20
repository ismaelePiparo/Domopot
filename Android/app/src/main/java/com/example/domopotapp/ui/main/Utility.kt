package com.example.domopotapp.ui.main

import android.content.Context
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.example.domopotapp.R
import java.io.InputStream

val defaultFirebaseOnFailureListener =
    { it: Exception -> Log.e("firebase", "Error getting data", it) }

fun linkAssetImage(imageView: ImageView, fileName: String) {
    val assetManager: AssetManager = imageView.context.assets
    val ims: InputStream = assetManager.open(fileName)
    val d = Drawable.createFromStream(ims, null)
    imageView.setImageDrawable(d)
}

fun getLastWateringFromTimestamp(timestamp: Int): Int {
    //TODO implement function
    return 2
}

fun getConnectionStatusFromTimestamp(timestamp: Int): Boolean {
    //TODO implement function
    return true
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