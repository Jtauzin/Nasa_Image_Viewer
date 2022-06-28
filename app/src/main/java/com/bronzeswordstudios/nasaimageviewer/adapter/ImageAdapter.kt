package com.bronzeswordstudios.nasaimageviewer.adapter

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isEmpty
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bronzeswordstudios.nasaimageviewer.R
import com.bronzeswordstudios.nasaimageviewer.network.model.ImageData
import com.bronzeswordstudios.nasaimageviewer.network.model.NasaImage
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
			setPaletteToView(imageData.palette!!, holder)
			if (!imageData.imageLabels.isNullOrEmpty() && holder.chipGroup.isEmpty()) {
				setChips(imageData.imageLabels, holder, imageData.palette!!)
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
		// Load the image, and the assets (such as palette and ML ID chips) that are associated
		changeVisibility(View.INVISIBLE, holder)
		val imageData = nasaImages[position].images[0]
		Picasso.get().load(url).error(R.drawable.image_error).into(holder.nasaImage, object : Callback {
			override fun onSuccess() {
				val image: Bitmap = (holder.nasaImage.drawable as BitmapDrawable).bitmap
				imageData.palette = createPalette(image)
				setPaletteToView(imageData.palette!!, holder)
				getLabels(image, holder, imageData)
				imageData.srcImage = holder.nasaImage.drawable
				changeVisibility(View.VISIBLE, holder)
			}

			override fun onError(e: Exception?) {
				// try to load the original URL if higher res fails
				Picasso.get().load(backupURL).error(R.drawable.image_error).into(holder.nasaImage, object : Callback {
					override fun onSuccess() {
						val image: Bitmap = (holder.nasaImage.drawable as BitmapDrawable).bitmap
						imageData.palette = createPalette(image)
						setPaletteToView(imageData.palette!!, holder)
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
		// swap UI visibility based on input
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
		// manipulate URL to pull higher res picture if available
		val urlSplit = inputString.split("thumb").toTypedArray()
		return urlSplit[0] + "large.jpg"
	}


	private fun getLabels(image: Bitmap, holder: ViewHolder, imageData: ImageData) {
		// extract labels from the image using the firebase ML functions
		val inputImage: InputImage = InputImage.fromBitmap(image, 0)
		val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
		labeler.process(inputImage).addOnSuccessListener { labels ->
			imageData.imageLabels = labels
			setChips(labels, holder, imageData.palette!!)
		}
			.addOnFailureListener { e ->
				Log.e("ERROR LABEL: ", "getLabels: $e")
			}
	}

	private fun setChips(labels: MutableList<ImageLabel>, holder: ViewHolder, palette: Palette) {
		// set the chip views with labels, if applicable
		if (labels.size > 0) {
			Thread(Runnable {
				for (label in labels) {
					val chip = Chip(context)
					chip.text = label.text
					if (palette.vibrantSwatch != null) {
						chip.chipBackgroundColor = ColorStateList.valueOf(palette.vibrantSwatch!!.rgb)
					}
					activity.runOnUiThread(Runnable {
						holder.chipGroup.addView(chip)
					})
				}
			}).start()
		}
	}

	fun createPalette(bitmap: Bitmap): Palette {
		return Palette.from(bitmap).generate()
	}

	private fun setPaletteToView(palette: Palette, holder: ViewHolder) {
		// Here we set up our color scheme of the card based on the palette pulled from the image
		if (palette.darkMutedSwatch?.rgb != null) {
			holder.titleView.setTextColor(palette.darkMutedSwatch!!.rgb)
		}
		if (palette.darkVibrantSwatch?.rgb != null) {
			holder.authorView.setTextColor(palette.darkVibrantSwatch!!.rgb)
			holder.dateView.setTextColor(palette.darkVibrantSwatch!!.rgb)
		}
		if (palette.dominantSwatch?.rgb != null) {
			holder.chipGroup.setBackgroundColor(palette.dominantSwatch!!.rgb)
		}
		if (palette.lightVibrantSwatch?.rgb != null) {
			holder.baseView.setBackgroundColor(palette.lightVibrantSwatch!!.rgb)
		} else if (palette.lightMutedSwatch?.rgb != null) {
			holder.baseView.setBackgroundColor(palette.lightMutedSwatch!!.rgb)
		}
	}

}