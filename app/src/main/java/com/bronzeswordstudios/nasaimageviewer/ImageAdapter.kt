package com.bronzeswordstudios.nasaimageviewer

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream
import java.net.URL

class ImageAdapter(private val imageList: ArrayList<ImageObj>, private val activity: Activity) :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    lateinit var context: Context
    lateinit var parentView: View

    // this list is to store our drawables once loaded
    private var drawableList: Array<Drawable?> = arrayOfNulls(imageList.size)

    class ViewHolder(adapterView: View) : RecyclerView.ViewHolder(adapterView) {
        // define a custom ViewHolder class since we have custom needs
        val titleView: TextView = adapterView.findViewById(R.id.title_view)
        val dateView: TextView = adapterView.findViewById(R.id.date_view)
        val nasaImage: ImageView = adapterView.findViewById(R.id.nasa_image)
        val authorView: TextView = adapterView.findViewById(R.id.center_view)
        val spinningLoader: ProgressBar = adapterView.findViewById(R.id.spinning_loader)
    }

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
        return imageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // set imageObj attributes here
        val titleView: TextView = holder.titleView
        val dateView: TextView = holder.dateView
        val authorView: TextView = holder.authorView
        val imageObj: ImageObj = imageList[position]
        val titleText: String =
                context.resources.getString(R.string.title) + " " + imageObj.getTitle()
        val authorText: String =
                context.resources.getString(R.string.author) + " " + imageObj.getCenter()
        val dateText: String =
                context.resources.getString(R.string.date) + " " + extractDate(imageObj.getDate())
        titleView.text = titleText
        dateView.text = dateText
        authorView.text = authorText
        loadImage(holder, imageObj.getURL(), imageObj.getBackupURL(), position)
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
        holder.nasaImage.visibility = View.INVISIBLE
        holder.spinningLoader.visibility = View.VISIBLE
        var drawable: Drawable?

        // check if we need to load the drawable or pull from list
        if (drawableList.getOrNull(position) == null) {
            Thread(Runnable {
                drawable = try {
                    val input: InputStream = URL(url).content as InputStream
                    Drawable.createFromStream(input, url)
                } catch (e: Exception) {
                    try {
                        // if no higher res picture is available, return the low res picture
                        val input: InputStream = URL(backupURL).content as InputStream
                        Drawable.createFromStream(input, backupURL)
                    } catch (e: Exception) {
                        // if there is no high res JPG or backup picture, display error picture
                        AppCompatResources.getDrawable(context, R.drawable.image_error)
                    }
                }
                // save drawable to list so we do not have to load again
                drawableList[position] = drawable
                activity.runOnUiThread {
                    // set UI via UI thread
                    holder.nasaImage.setImageDrawable(drawable)
                    holder.nasaImage.visibility = View.VISIBLE
                    holder.spinningLoader.visibility = View.INVISIBLE
                }
            }).start()
        } else {
            // display saved image if we have one
            drawable = drawableList[position]
            holder.nasaImage.setImageDrawable(drawable)
            holder.nasaImage.visibility = View.VISIBLE
            holder.spinningLoader.visibility = View.INVISIBLE
        }


    }

    private fun extractDate(rawString: String?): String? {
        // NASA's date/time stamp format is <date>T<time> so we can split these values
        val stringArray = rawString?.split("T")
        return stringArray?.get(0)
    }

}