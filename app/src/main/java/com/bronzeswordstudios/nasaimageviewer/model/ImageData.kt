package com.bronzeswordstudios.nasaimageviewer.model

import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.mlkit.vision.label.ImageLabel

// using kotlin due to android.graphics library import issue while using .java class
class ImageData(@field:Expose @field:SerializedName("href") var href: String) {
	var srcImage: Drawable? = null
	var imageLabels: MutableList<ImageLabel> = mutableListOf()
	var palette: Palette? = null
}