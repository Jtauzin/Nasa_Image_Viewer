package com.bronzeswordstudios.nasaimageviewer.adapter

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bronzeswordstudios.nasaimageviewer.R

class ViewHolder(adapterView: View) : RecyclerView.ViewHolder(adapterView) {
	// define a custom ViewHolder class since we have custom needs
	val titleView: TextView = adapterView.findViewById(R.id.title_view)
	val dateView: TextView = adapterView.findViewById(R.id.date_view)
	val nasaImage: ImageView = adapterView.findViewById(R.id.nasa_image)
	val authorView: TextView = adapterView.findViewById(R.id.center_view)
	val spinningLoader: ProgressBar = adapterView.findViewById(R.id.spinning_loader)
}