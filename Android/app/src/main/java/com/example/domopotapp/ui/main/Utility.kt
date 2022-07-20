package com.example.domopotapp.ui.main

import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.RequiresApi
import java.io.InputStream

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
    imageView.imageTintList = ColorStateList.valueOf(context.resources.getColor(colorId, context.theme))
}