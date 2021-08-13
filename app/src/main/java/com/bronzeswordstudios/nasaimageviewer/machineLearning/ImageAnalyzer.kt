package com.bronzeswordstudios.nasaimageviewer.machineLearning

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class ImageAnalyzer(image: Bitmap) {
	private val inputImage: InputImage = InputImage.fromBitmap(image, 0)
	private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
	private var labels: MutableList<ImageLabel>? = null


	fun getLabels(): MutableList<ImageLabel>? {
		labeler.process(inputImage).addOnSuccessListener { labels ->
			this.labels = labels
		}
			.addOnFailureListener { e ->
				Log.e("ERROR LABEL: ", "getLabels: $e")
			}
		return labels
	}
}