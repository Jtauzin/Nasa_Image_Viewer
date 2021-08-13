package com.bronzeswordstudios.nasaimageviewer.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isEmpty
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.bronzeswordstudios.nasaimageviewer.R
import com.bronzeswordstudios.nasaimageviewer.model.ImageData
import com.bronzeswordstudios.nasaimageviewer.model.NasaImage
import com.google.android.material.chip.Chip
import com.google.android.material.textview.MaterialTextView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ImageAdapter(
	private val nasaImages: ArrayList<NasaImage>,
	private val activity: Activity
) :
	RecyclerView.Adapter<ViewHolder>() {

	lateinit var context: Context

	//--------------------------------------------------------------------------------------------//
	/* begin our override methods here*/
	//--------------------------------------------------------------------------------------------//

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		// initialize variables and inflate / return our ViewHolder
		context = parent.context
		val inflater: LayoutInflater = LayoutInflater.from(context)
		val imageView = inflater.inflate(R.layout.adapter_view, parent, false)

		return ViewHolder(imageView)
	}


	override fun getItemCount(): Int {
		// item count should equal the list size
		return nasaImages.size
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		// find attributes here
		val item = nasaImages[position]
		val genData = item.data[0]
		val imageData = item.images[0]
		val url = imageData.href

		// set attributes
		val titleView: MaterialTextView = holder.titleView
		val dateView: MaterialTextView = holder.dateView
		val authorView: MaterialTextView = holder.authorView
		val authorText: String = context.resources.getString(R.string.author) + " " + genData.center
		titleView.text = genData.title
		dateView.text = extractDate(genData.dateCreated)
		authorView.text = authorText

		// load image if we do not already have one attached to our NasaImage object
		if (imageData.srcImage == null) {
			loadImage(holder, adjustURL(url), url, position)
		} else {
			holder.nasaImage.setImageDrawable(imageData.srcImage)
			changeVisibility(View.VISIBLE, holder)
			if (!imageData.imageLabels.isNullOrEmpty() && holder.chipGroup.isEmpty()) {
				setChips(imageData.imageLabels, holder)
			}
		}
	}

	/* getItemID and getItemViewType overrides correct an issue with fast scrolling on recycler view.
	* sometimes when a user scrolls very fast, an incorrect image may render from mem. It is replaced
	* quickly by the correct image, but the user could notice. Changing these methods from their
	* "super.function(position)" return value to the position value corrects this.*/
	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	override fun getItemViewType(position: Int): Int {
		return position
	}

	//--------------------------------------------------------------------------------------------//
	/* begin our private functions here*/
	//--------------------------------------------------------------------------------------------//

	private fun loadImage(holder: ViewHolder, url: String, backupURL: String, position: Int) {
		// set up loading display
		changeVisibility(View.INVISIBLE, holder)
		val imageData = nasaImages[position].images[0]
		// check if we need to load the drawable or pull from list
		Picasso.get().load(url).error(R.drawable.image_error).into(holder.nasaImage, object : Callback {
			override fun onSuccess() {
				val image: Bitmap = (holder.nasaImage.drawable as BitmapDrawable).bitmap
				getLabels(image, holder, imageData)
				imageData.srcImage = holder.nasaImage.drawable
				changeVisibility(View.VISIBLE, holder)
			}

			override fun onError(e: Exception?) {
				Picasso.get().load(backupURL).error(R.drawable.image_error).into(holder.nasaImage, object : Callback {
					override fun onSuccess() {
						val image: Bitmap = (holder.nasaImage.drawable as BitmapDrawable).bitmap
						getLabels(image, holder, imageData)
						imageData.srcImage = holder.nasaImage.drawable
						changeVisibility(View.VISIBLE, holder)
					}

					override fun onError(e: Exception?) {
						changeVisibility(View.VISIBLE, holder)
					}
				})
			}
		})
	}

	private fun extractDate(rawString: String?): String? {
		// NASA's date/time stamp format is <date>T<time> so we can split these values
		val stringArray = rawString?.split("T")
		return stringArray?.get(0)
	}

	private fun changeVisibility(visibility: Int, holder: ViewHolder) {
		when (visibility) {
			View.INVISIBLE -> {
				holder.nasaImage.visibility = View.INVISIBLE
				holder.spinningLoader.visibility = View.VISIBLE
			}

			View.VISIBLE -> {
				holder.nasaImage.visibility = View.VISIBLE
				holder.spinningLoader.visibility = View.INVISIBLE
			}
		}
	}

	private fun adjustURL(inputString: String): String {
		val urlSplit = inputString.split("thumb").toTypedArray()
		return urlSplit[0] + "large.jpg"
	}


	private fun getLabels(image: Bitmap, holder: ViewHolder, imageData: ImageData) {
		val inputImage: InputImage = InputImage.fromBitmap(image, 0)
		val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
		labeler.process(inputImage).addOnSuccessListener { labels ->
			imageData.imageLabels = labels
			setChips(labels, holder)
		}
			.addOnFailureListener { e ->
				Log.e("ERROR LABEL: ", "getLabels: $e")
			}
	}

	private fun setChips(labels: MutableList<ImageLabel>, holder: ViewHolder) {
		if (labels.size > 0) {
			Thread(Runnable {
				for (label in labels) {
					val chip = Chip(context)
					chip.text = label.text
					activity.runOnUiThread(Runnable {
						holder.chipGroup.addView(chip)
					})
				}
			}).start()
		}
	}

}