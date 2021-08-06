package com.bronzeswordstudios.nasaimageviewer.model

import android.graphics.drawable.Drawable

class NasaImage(
	// our ImageObj class holds our information for each image to be displayed
	private val url: String?,
	private val title: String?,
	private val center: String?,
	private val date: String?,
	private val backupURL: String?
) {

	// we will store each picture as it is loaded to avoid reloading images later.
	private var image: Drawable? = null


	fun getDate(): String {
		if (date == null) {
			return "Date not available"
		}
		return date
	}

	fun getTitle(): String {
		if (title == null) {
			return "Title not available"
		}
		return title
	}

	fun getURL(): String {
		if (url == null) {
			return "URL not available"
		}
		return url
	}

	fun getBackupURL(): String {
		if (backupURL == null) {
			return "URL not available"
		}
		return backupURL
	}

	fun getCenter(): String {
		if (center == null) {
			return "center not available"
		}
		return center
	}

	fun getImage(): Drawable? {
		return image
	}

	fun setImage(image: Drawable) {
		this.image = image
	}
}