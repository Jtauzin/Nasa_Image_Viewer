package com.bronzeswordstudios.nasaimageviewer.adapter

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bronzeswordstudios.nasaimageviewer.R
import com.google.android.material.textview.MaterialTextView

class ViewHolder(adapterView: View) : RecyclerView.ViewHolder(adapterView) {
	// define a custom ViewHolder class since we have custom needs
	val titleView: MaterialTextView = adapterView.findViewById(R.id.title_view)
	val dateView: MaterialTextView = adapterView.findViewById(R.id.date_view)
	val nasaImage: ImageView = adapterView.findViewById(R.id.nasa_image)
	val authorView: MaterialTextView = adapterView.findViewById(R.id.center_view)
	val spinningLoader: ProgressBar = adapterView.findViewById(R.id.spinning_loader)
}