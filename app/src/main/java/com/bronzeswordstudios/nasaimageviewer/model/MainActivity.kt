package com.bronzeswordstudios.nasaimageviewer.model

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bronzeswordstudios.nasaimageviewer.R
import com.bronzeswordstudios.nasaimageviewer.adapter.ImageAdapter
import com.bronzeswordstudios.nasaimageviewer.network.QueryLoader
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<ArrayList<NasaImage>>,
	SearchFragment.SearchDialogListener {

	// set up globals
	private lateinit var mLoaderManager: LoaderManager
	private lateinit var imageRecyclerView: RecyclerView
	private lateinit var errorView: TextView
	private lateinit var imageAdapter: ImageAdapter
	private lateinit var primaryProgressBar: ProgressBar
	private var isSearch: Boolean = false
	private var searchUrl: String = ""
	private var urlList: ArrayList<String> = ArrayList()
	private val loaderID = 0
	private var index = 0
	private lateinit var connectivityManager: ConnectivityManager

	// Using this as a static object allows us to retain the values through a screen orientation
	// change
	companion object {
		var imageList = ArrayList<NasaImage>()
		var isLoaded: Boolean = false                    // this boolean is used so we do not reload data on focus change and return
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		connectivityManager = getSystemService(ConnectivityManager::class.java)

		val appBar: MaterialToolbar = findViewById(R.id.app_bar)
		val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
		val navView: NavigationView = findViewById(R.id.nav_view)

		primaryProgressBar = findViewById(R.id.primary_progress_bar)
		errorView = findViewById(R.id.error_view)
		mLoaderManager = LoaderManager.getInstance(this)

		// create a list of potential urls to request for variety
		urlList.add("https://images-api.nasa.gov/search?q=galaxy&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=solar&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=asteroid&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=UFO&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=moon&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=mars+rover&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=mercury&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=venus&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=earth&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=mars&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=jupiter&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=saturn&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=uranus&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=neptune&media_type=image")
		urlList.add("https://images-api.nasa.gov/search?q=pluto&media_type=image")

		imageRecyclerView = findViewById(R.id.recycle_view)
		imageRecyclerView.layoutManager = LinearLayoutManager(this)
		if (!hasConnectivity()) {
			// check connectivity in case of rotation, we need to update screen if connection was lost
			errorView.text = resources.getString(R.string.no_connection_text)
			primaryProgressBar.visibility = View.INVISIBLE
		}

		// if we still have values in our image list, we need to go ahead and load it here in case connection is restored later while in use
		if (imageList.size != 0) {
			// for screen rotation, on create is called again so we set the adapter to the
			// prev. values
			primaryProgressBar.visibility = View.INVISIBLE
			imageAdapter = ImageAdapter(imageList, this)
			imageRecyclerView.adapter = imageAdapter
		}


		// set refresh layout logic
		val layoutRefresher: SwipeRefreshLayout = findViewById(R.id.swipe_refresh)
		layoutRefresher.setOnRefreshListener {

			// call restart loader and cease refreshing indicator
			restartLoader()
			layoutRefresher.isRefreshing = false
		}

		// handle nav drawer logic here
		appBar.setNavigationOnClickListener {
			drawerLayout.openDrawer(GravityCompat.START)
		}

		navView.setNavigationItemSelectedListener { menuItem ->
			if (menuItem.itemId == R.id.search) {
				displaySearch()
			}
			drawerLayout.closeDrawer(GravityCompat.START)
			true
		}

		// lets listen for connectivity changes here, and adjust our UI accordingly *just for kicks*
		connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
			override fun onAvailable(network: Network) {
				runOnUiThread(Runnable {
					if (!isLoaded) {
						startLoader()
					}
					errorView.visibility = View.INVISIBLE
					imageRecyclerView.visibility = View.VISIBLE
				})
			}

			override fun onLost(network: Network) {
				runOnUiThread(Runnable {
					errorView.visibility = View.VISIBLE
					imageRecyclerView.visibility = View.INVISIBLE
				})
			}
		})
	}

	//--------------------------------------------------------------------------------------------//
	/* begin our loader override methods here*/
	//--------------------------------------------------------------------------------------------//

	override fun onCreateLoader(id: Int, args: Bundle?): Loader<ArrayList<NasaImage>> {
		// we are loading a new stream, so reset our loaded boolean
		isLoaded = false
		// pick a random url from our urlList
		return QueryLoader(this, urlSelectionHandler(isSearch))
	}


	override fun onLoadFinished(loader: Loader<ArrayList<NasaImage>>, data: ArrayList<NasaImage>?) {
		// if we have data to show, set the adapters and away we go!
		if (data != null) {
			if (data.size == 0 && imageList.size != 0 && hasConnectivity()) {
				// the boolean above means the next data pull was not successful
				// (either search or refresh)
				val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator_layout)
				val snackbar =
					Snackbar.make(coordinatorLayout, R.string.no_results, Snackbar.LENGTH_LONG)

				//add a dismiss option on the popup here
				snackbar.setAction(R.string.dismiss) {
					snackbar.dismiss()
				}

				snackbar.show()
			} else if (!isLoaded) {
				imageList = data
				imageAdapter = ImageAdapter(imageList, this)
				imageRecyclerView.adapter = imageAdapter
				imageRecyclerView.visibility = View.VISIBLE
				errorView.visibility = View.INVISIBLE
				primaryProgressBar.visibility = View.INVISIBLE
				// we have loaded our data, now we can declare it as loaded.
				isLoaded = true
			}
		}
	}


	override fun onLoaderReset(loader: Loader<ArrayList<NasaImage>>) {
		imageRecyclerView.adapter = null
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.search -> {
				// display our dialog box
				displaySearch()

			}
		}
		return true
	}

	// for our search dialog pop up
	override fun onDialogPositiveClick(dialog: SearchFragment) {
		performSearch(dialog.getString())
	}

	//--------------------------------------------------------------------------------------------//
	/* begin our custom methods here*/
	//--------------------------------------------------------------------------------------------//

	private fun performSearch(searchString: String) {
		// just for some fun, lets give the user the option to search for images
		// set isSearch to true so our URL selector will know what to do
		val urlList: ArrayList<String> = ArrayList()
		isSearch = true

		// start with the base search URL split into 2 to build upon
		urlList.add("https://images-api.nasa.gov/search?")
		urlList.add("&media_type=image")
		searchUrl = ""

		// if the user did not input anything. Behave like normal
		if (searchString == "") {
			isSearch = false
			restartLoader()
		}

		// else split the search parameters into an array and add them to the query
		else {
			val textToSearch = searchString.split(" ")
			searchUrl += urlList[0]
			for ((i, word) in textToSearch.withIndex()) {
				if (i == 0) {
					searchUrl += "q=$word"
				}
				if (i > 0) {
					searchUrl += word
				}
				if (i < textToSearch.size - 1) {
					searchUrl += "+"
				}
			}

			// complete the URL and load the new images!
			searchUrl += urlList[1]
			restartLoader()
		}
	}

	private fun urlSelectionHandler(isSearch: Boolean): String {
		return if (isSearch) {
			this.isSearch = false
			searchUrl
		} else {
			// here we adjust our URL from our selections
			if (index >= urlList.size) {
				// if our index exceeds our list length reset
				index = 0
			}
			val urlToReturn = urlList[index]
			index += 1
			urlToReturn
		}
	}

	private fun hasConnectivity(): Boolean {
		// check to see if we have connectivity. Returns true or false
		val activeNetwork = connectivityManager.activeNetwork
		val capabilities: NetworkCapabilities? =
			connectivityManager.getNetworkCapabilities(activeNetwork)
		var returnBoolean: Boolean? =
			capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
		if (returnBoolean == null) {
			returnBoolean = false
		}
		return returnBoolean
	}

	private fun restartLoader() {
		// handles loader reset logic:
		// UI updates, loader reset, and connectivity check for a loader restart
		if (hasConnectivity()) {
			mLoaderManager.restartLoader(loaderID, null, this)
			errorView.visibility = View.INVISIBLE
			imageRecyclerView.visibility = View.VISIBLE
		} else {
			errorView.text = resources.getString(R.string.no_connection_text)
			errorView.visibility = View.VISIBLE
			imageRecyclerView.visibility = View.INVISIBLE
		}
	}

	private fun startLoader() {
		// create connectivity check
		if (hasConnectivity()) {
			// if connected load items, else display connection error message
			mLoaderManager.initLoader(loaderID, null, this)
		} else {
			errorView.visibility = View.VISIBLE
			primaryProgressBar.visibility = View.INVISIBLE
		}
	}

	private fun displaySearch() {
		val searchDialog = SearchFragment()
		searchDialog.show(supportFragmentManager, "search")
	}

	//--------------------------------------------------------------------------------------------//
	/* lifecycle overrides here*/
	//--------------------------------------------------------------------------------------------//

	override fun onResume() {
		if (hasConnectivity()) {
			errorView.visibility = View.INVISIBLE
			imageRecyclerView.visibility = View.VISIBLE
		} else {
			errorView.visibility = View.VISIBLE
			imageRecyclerView.visibility = View.INVISIBLE
		}
		super.onResume()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		outState.putInt("index", index)
		super.onSaveInstanceState(outState)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		index = savedInstanceState.getInt("index")
	}


}