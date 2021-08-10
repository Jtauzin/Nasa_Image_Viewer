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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bronzeswordstudios.nasaimageviewer.R
import com.bronzeswordstudios.nasaimageviewer.adapter.ImageAdapter
import com.bronzeswordstudios.nasaimageviewer.network.API
import com.bronzeswordstudios.nasaimageviewer.network.RetroFitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(),
	SearchFragment.SearchDialogListener {

	// set up globals
	private lateinit var mLoaderManager: LoaderManager
	private lateinit var imageRecyclerView: RecyclerView
	private lateinit var errorView: TextView
	private lateinit var imageAdapter: ImageAdapter
	private lateinit var primaryProgressBar: ProgressBar
	private var isSearch: Boolean = false
	private val ERROR = 8140146
	private var index = 0
	private lateinit var connectivityManager: ConnectivityManager

	// Using this as a static object allows us to retain the values through a screen orientation
	// change
	companion object {
		var imageList = ArrayList<NasaImage>()
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

		imageRecyclerView = findViewById(R.id.recycle_view)
		imageRecyclerView.layoutManager = LinearLayoutManager(this)

		// if we still have values in our image list, we need to go ahead and load it here in case connection is restored later while in use
		if (imageList.size != 0) {
			// for screen rotation, on create is called again so we set the adapter to the
			// prev. values
			imageAdapter = ImageAdapter(imageList)
			imageRecyclerView.adapter = imageAdapter
			adjustVisibility(View.VISIBLE)
		} else if (!hasConnectivity()) {
			// check connectivity in case of rotation, we need to update screen if connection was lost
			adjustVisibility(ERROR)
		}

		// set refresh layout logic
		val layoutRefresher: SwipeRefreshLayout = findViewById(R.id.swipe_refresh)
		layoutRefresher.setOnRefreshListener {

			// call restart loader and cease refreshing indicator
			index += 1
			if (index == 15) {
				index = 0
			}
			callRetrofit()
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
					if (imageList.size == 0) {
						// delay here for change in connectivity. When connection reestablished it can take a second
						// to have access to that new connection even though it has been detected.
						Thread.sleep(1000)
						callRetrofit()
					}
					adjustVisibility(View.VISIBLE)
				})
			}

			override fun onLost(network: Network) {
				runOnUiThread(Runnable {
					createSnackBar(R.string.connection_lost)
					if (imageList.size == 0) {
						adjustVisibility(ERROR)
					}
				})
			}
		})
		if (imageList.size == 0) {
			callRetrofit()
		}
	}

	//--------------------------------------------------------------------------------------------//
	/* Non-lifecycle override methods here*/
	//--------------------------------------------------------------------------------------------//

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
		callRetrofit(dialog.getString())
	}

	//--------------------------------------------------------------------------------------------//
	/* begin our custom methods here*/
	//--------------------------------------------------------------------------------------------//

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

	private fun displaySearch() {
		val searchDialog = SearchFragment()
		searchDialog.show(supportFragmentManager, "search")
	}

	private fun createSnackBar(resource: Int) {
		val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator_layout)
		val snackbar = Snackbar.make(coordinatorLayout, resource, Snackbar.LENGTH_LONG)
		//add a dismiss option on the popup here
		snackbar.setAction(R.string.dismiss) {
			snackbar.dismiss()
		}
		snackbar.show()
	}

	private fun adjustVisibility(visibility: Int) {
		when (visibility) {
			View.VISIBLE -> {
				errorView.visibility = View.INVISIBLE
				primaryProgressBar.visibility = View.INVISIBLE
				imageRecyclerView.visibility = View.VISIBLE
			}
			View.INVISIBLE -> {
				errorView.visibility = View.INVISIBLE
				primaryProgressBar.visibility = View.VISIBLE
				imageRecyclerView.visibility = View.INVISIBLE
			}
			ERROR -> {
				errorView.text = resources.getString(R.string.no_connection_text)
				errorView.visibility = View.VISIBLE
				primaryProgressBar.visibility = View.INVISIBLE
				imageRecyclerView.visibility = View.INVISIBLE
			}
		}
	}

	private fun callRetrofit() {
		// retrofit implementation
		val api: API = RetroFitClient.getRetrofitInstance().create(API::class.java)
		var call: Call<DataResult>? = null
		when (index) {
			0 -> call = api.variety0
			1 -> call = api.variety1
			2 -> call = api.variety2
			3 -> call = api.variety3
			4 -> call = api.variety4
			5 -> call = api.variety5
			6 -> call = api.variety6
			7 -> call = api.variety7
			8 -> call = api.variety8
			9 -> call = api.variety9
			10 -> call = api.variety10
			11 -> call = api.variety11
			12 -> call = api.variety12
			13 -> call = api.variety13
			14 -> call = api.variety14
		}

		call?.enqueue(object : Callback<DataResult> {
			override fun onResponse(p0: Call<DataResult>, p1: Response<DataResult>) {
				if (p1.body() != null) {
					imageList = p1.body()!!.nasaImages
					imageAdapter = ImageAdapter(imageList)
					imageRecyclerView.adapter = imageAdapter
				}
			}

			override fun onFailure(p0: Call<DataResult>, p1: Throwable) {
				createSnackBar(R.string.load_result_error)
			}
		})
	}

	private fun callRetrofit(query: String) {
		// retrofit implementation
		if (query == "") {
			return
		}
		val api: API = RetroFitClient.getRetrofitInstance().create(API::class.java)
		var call: Call<DataResult>? = null
		call = api.getSearch(query, "image")
		isSearch = false

		call?.enqueue(object : Callback<DataResult> {
			override fun onResponse(p0: Call<DataResult>, p1: Response<DataResult>) {
				imageList = p1.body()!!.nasaImages
				if (imageList.size == 0) {
					createSnackBar(R.string.no_results)
					return
				}
				imageAdapter = ImageAdapter(imageList)
				imageRecyclerView.adapter = imageAdapter
			}

			override fun onFailure(p0: Call<DataResult>, p1: Throwable) {
				createSnackBar(R.string.load_result_error)
			}
		})
	}

	//--------------------------------------------------------------------------------------------//
	/* lifecycle overrides here*/
	//--------------------------------------------------------------------------------------------//

	override fun onSaveInstanceState(outState: Bundle) {
		outState.putInt("index", index)
		super.onSaveInstanceState(outState)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		index = savedInstanceState.getInt("index")
	}


}