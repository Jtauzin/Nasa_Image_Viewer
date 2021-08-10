package com.bronzeswordstudios.nasaimageviewer.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bronzeswordstudios.nasaimageviewer.R
import com.bronzeswordstudios.nasaimageviewer.model.NasaImage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ImageAdapter(
	private val nasaImageList: ArrayList<NasaImage>
) :
	RecyclerView.Adapter<ViewHolder>() {

	lateinit var context: Context
	lateinit var parentView: View

	//--------------------------------------------------------------------------------------------//
	/* begin our override methods here*/
	//--------------------------------------------------------------------------------------------//

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		// initialize variables and inflate / return our ViewHolder
		context = parent.context
		val inflater: LayoutInflater = LayoutInflater.from(context)
		val imageView = inflater.inflate(R.layout.adapter_view, parent, false)
		parentView = imageView
		return ViewHolder(imageView)
	}


	override fun getItemCount(): Int {
		// item count should equal the list size
		return nasaImageList.size
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		// set imageObj attributes here
		val titleView: TextView = holder.titleView
		val dateView: TextView = holder.dateView
		val authorView: TextView = holder.authorView
		val nasaImage: NasaImage = nasaImageList[position]
		val titleText: String =
			context.resources.getString(R.string.title) + " " + nasaImage.getTitle()
		val authorText: String =
			context.resources.getString(R.string.author) + " " + nasaImage.getCenter()
		val dateText: String =
			context.resources.getString(R.string.date) + " " + extractDate(nasaImage.getDate())
		titleView.text = titleText
		dateView.text = dateText
		authorView.text = authorText

		// load image if we do not already have one attached to our NasaImage object
		if (nasaImage.getImage() == null) {
			loadImage(holder, nasaImage.getURL(), nasaImage.getBackupURL(), position)
		} else {
			holder.nasaImage.setImageDrawable(nasaImage.getImage())
			changeVisibility(View.VISIBLE, holder)
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

		// check if we need to load the drawable or pull from list
		Picasso.get().load(url).error(R.drawable.image_error).into(holder.nasaImage, object : Callback {
			override fun onSuccess() {
				nasaImageList[position].setImage(holder.nasaImage.drawable)
				changeVisibility(View.VISIBLE, holder)
			}

			override fun onError(e: Exception?) {
				Picasso.get().load(backupURL).error(R.drawable.image_error).into(holder.nasaImage, object : Callback {
					override fun onSuccess() {
						nasaImageList[position].setImage(holder.nasaImage.drawable)
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

	private fun changeVisibility(visibility: Int, holder: ViewHolder){
		when (visibility){
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

}