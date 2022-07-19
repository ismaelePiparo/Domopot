package com.example.domopotapp.ui.main

import android.content.res.AssetManager
import android.graphics.drawable.Drawable
import android.widget.ImageView
import java.io.InputStream

fun linkAssetImage(imageView: ImageView, fileName: String) {
    val assetManager: AssetManager = imageView.context.assets
    val ims: InputStream = assetManager.open(fileName)
    val d = Drawable.createFromStream(ims, null)
    imageView.setImageDrawable(d)
}