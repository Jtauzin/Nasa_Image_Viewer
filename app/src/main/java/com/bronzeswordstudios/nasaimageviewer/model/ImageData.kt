package com.bronzeswordstudios.nasaimageviewer.model

import android.graphics.drawable.Drawable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

// using kotlin due to android.graphics library import issue while using .java class
class ImageData(@field:Expose @field:SerializedName("href") var href: String) {
	var srcImage: Drawable? = null

}