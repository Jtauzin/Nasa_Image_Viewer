package com.bronzeswordstudios.nasaimageviewer

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<ArrayList<ImageObj>> {

    // set up globals
    private lateinit var mLoaderManager: LoaderManager
    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var errorView: TextView
    private lateinit var searchPopUp: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var imageAdapter: ImageAdapter
    private var isSearch: Boolean = false
    private var searchUrl: String = ""
    private var urlList: ArrayList<String> = ArrayList()
    private var index: Int = 0
    private val loaderID = 0

    companion object{
        var imageList = ArrayList<ImageObj>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val submitSearch: Button = findViewById(R.id.submit_search)
        searchPopUp = findViewById(R.id.search_popup)
        errorView = findViewById(R.id.error_view)
        searchInput = findViewById(R.id.search_input)

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
        if (imageList.isEmpty()){
            startLoader()
        }
        else{
            // for screen rotation, on create is called again so we set the adapter to the
            // prev. values
            imageAdapter = ImageAdapter(imageList, this)
            imageRecyclerView.adapter = imageAdapter
            imageRecyclerView.visibility = View.VISIBLE
        }


        // set refresh layout logic
        val layoutRefresher: SwipeRefreshLayout = findViewById(R.id.swipe_refresh)
        layoutRefresher.setOnRefreshListener {

            // call restart loader and cease refreshing indicator
            restartLoader()
            layoutRefresher.isRefreshing = false
        }

        // handle search submission
        submitSearch.setOnClickListener {
            searchPopUp.visibility = View.GONE
            performSearch(searchInput.text.toString())
            searchInput.setText("")
        }
    }

    //--------------------------------------------------------------------------------------------//
    /* begin our loader override methods here*/
    //--------------------------------------------------------------------------------------------//

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ArrayList<ImageObj>> {
        // pick a random url from our urlList
        return QueryLoader(this, urlSelectionHandler(isSearch))
    }


    override fun onLoadFinished(loader: Loader<ArrayList<ImageObj>>, data: ArrayList<ImageObj>?) {
        // if we have data to show, set the adapters and away we go!
        if (data != null) {
            if (data.size == 0 && imageList.size == 0) {
                imageRecyclerView.visibility = View.INVISIBLE
                errorView.text = resources.getString(R.string.no_results)
                errorView.visibility = View.VISIBLE
            } else {
                // if we have new data update the loader. Otherwise set the views.
                if (data.size > 0){
                    imageList = data
                    imageAdapter = ImageAdapter(imageList, this)
                    imageRecyclerView.adapter = imageAdapter
                }
                imageRecyclerView.visibility = View.VISIBLE
                errorView.visibility = View.INVISIBLE
            }
        }
    }


    override fun onLoaderReset(loader: Loader<ArrayList<ImageObj>>) {
        imageRecyclerView.adapter = null
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                val searchPopUp: LinearLayout = findViewById(R.id.search_popup)
                searchPopUp.visibility = View.VISIBLE

            }
        }
        return true
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


    private fun hasConnectivity(): Boolean? {
        // check to see if we have connectivity. Returns true, false, or null
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities: NetworkCapabilities? =
            connectivityManager.getNetworkCapabilities(activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun restartLoader() {
        // handles loader reset logic:
        // UI updates, loader reset, and connectivity check for a loader restart
        val appHasConnectivity: Boolean? = hasConnectivity()
        if (appHasConnectivity != null && appHasConnectivity == true) {
            mLoaderManager.restartLoader(loaderID, null, this)
            errorView.visibility = View.INVISIBLE
            imageRecyclerView.visibility = View.VISIBLE
        } else {
            errorView.text = resources.getString(R.string.no_connection_text)
            errorView.visibility = View.VISIBLE
            imageRecyclerView.visibility = View.INVISIBLE
        }
    }

    private fun startLoader(){
        // create connectivity check
        val appHasConnectivity = hasConnectivity()
        mLoaderManager = LoaderManager.getInstance(this)
        if (appHasConnectivity != null && appHasConnectivity == true) {
            // if connected load items, else display connection error message
            mLoaderManager.initLoader(loaderID, null, this)
        } else {
            errorView.visibility = View.VISIBLE
        }
    }

    //--------------------------------------------------------------------------------------------//
    /* Lifecycle overrides here*/
    //--------------------------------------------------------------------------------------------//

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}