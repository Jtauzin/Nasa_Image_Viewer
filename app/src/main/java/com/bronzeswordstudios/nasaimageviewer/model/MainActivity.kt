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
import com.bronzeswordstudios.nasaimageviewer.network.model.DataResult
import com.bronzeswordstudios.nasaimageviewer.network.model.NasaImage
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
	private val ERROR = 8140146
	private var index = 0
	private lateinit var connectivityManager: ConnectivityManager
	private lateinit var retroFitClient: RetroFitClient
	private var nasaImages: ArrayList<NasaImage> = ArrayList()

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
		retroFitClient = RetroFitClient(applicationContext)
		imageRecyclerView = findViewById(R.id.recycle_view)
		imageRecyclerView.layoutManager = LinearLayoutManager(this)

		// set refresh layout logic
		val layoutRefresher: SwipeRefreshLayout = findViewById(R.id.swipe_refresh)
		layoutRefresher.setOnRefreshListener {
			// call restart loader and cease refreshing indicator
			if (hasConnectivity()) {
				index += 1
				if (index == 15) {
					index = 0
				}
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
					if (nasaImages.isEmpty()) {
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
					if (nasaImages.isEmpty()) {
						adjustVisibility(ERROR)
					}
				})
			}
		})
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
		val api: API = retroFitClient.retrofitInstance.create(API::class.java)
		var call: Call<DataResult>? = null
		when (index) {
			0 -> call = api.getSearch(API.queryList[0])
			1 -> call = api.getSearch(API.queryList[1])
			2 -> call = api.getSearch(API.queryList[2])
			3 -> call = api.getSearch(API.queryList[3])
			4 -> call = api.getSearch(API.queryList[4])
			5 -> call = api.getSearch(API.queryList[5])
			6 -> call = api.getSearch(API.queryList[6])
			7 -> call = api.getSearch(API.queryList[7])
			8 -> call = api.getSearch(API.queryList[8])
			9 -> call = api.getSearch(API.queryList[9])
			10 -> call = api.getSearch(API.queryList[10])
			11 -> call = api.getSearch(API.queryList[11])
			12 -> call = api.getSearch(API.queryList[12])
			13 -> call = api.getSearch(API.queryList[13])
			14 -> call = api.getSearch(API.queryList[14])
		}

		call?.enqueue(object : Callback<DataResult> {
			override fun onResponse(p0: Call<DataResult>, p1: Response<DataResult>) {
				if (p1.body()!!.collection.items.isEmpty()) {
					createSnackBar(R.string.no_results)
					return
				}
				nasaImages = p1.body()!!.collection.items as ArrayList<NasaImage>
				imageAdapter = ImageAdapter(nasaImages, this@MainActivity)
				imageRecyclerView.adapter = imageAdapter
			}

			override fun onFailure(p0: Call<DataResult>, p1: Throwable) {
				createSnackBar(R.string.load_result_error)
				if (nasaImages.isEmpty()) {
					adjustVisibility(ERROR)
				}
			}
		})
	}

	private fun callRetrofit(query: String) {
		// retrofit implementation
		if (query == "") {
			return
		}
		val api: API = retroFitClient.retrofitInstance.create(API::class.java)
		val call: Call<DataResult>? = api.getSearch(query)

		call?.enqueue(object : Callback<DataResult> {
			override fun onResponse(p0: Call<DataResult>, p1: Response<DataResult>) {
				if (p1.body()!!.collection.items.isEmpty()) {
					createSnackBar(R.string.no_results)
					return
				}
				nasaImages = p1.body()!!.collection.items as ArrayList<NasaImage>
				imageAdapter = ImageAdapter(nasaImages, this@MainActivity)
				imageRecyclerView.adapter = imageAdapter
			}

			override fun onFailure(p0: Call<DataResult>, p1: Throwable) {
				createSnackBar(R.string.load_result_error)
				if (nasaImages.isEmpty()) {
					adjustVisibility(ERROR)
				}
			}
		})
	}

	//--------------------------------------------------------------------------------------------//
	/* lifecycle overrides here*/
	//--------------------------------------------------------------------------------------------//

	override fun onSaveInstanceState(outState: Bundle) {
		outState.putParcelableArrayList("list", nasaImages)
		outState.putInt("index", index)
		super.onSaveInstanceState(outState)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		// store data for reuse
		index = savedInstanceState.getInt("index")
		nasaImages = savedInstanceState.getParcelableArrayList<NasaImage>("list") as ArrayList<NasaImage>
	}

	override fun onResume() {
		if (nasaImages.isNotEmpty() && imageRecyclerView.adapter == null) {
			// for screen rotation, on create is called again so we set the adapter to the
			// prev. values
			imageAdapter = ImageAdapter(nasaImages, this)
			imageRecyclerView.adapter = imageAdapter
			adjustVisibility(View.VISIBLE)
		} else if (!hasConnectivity()) {
			// check connectivity in case of rotation, we need to update screen if connection was lost
			adjustVisibility(ERROR)
		}
		super.onResume()
	}

}