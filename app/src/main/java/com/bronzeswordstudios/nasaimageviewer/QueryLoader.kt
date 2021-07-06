package com.bronzeswordstudios.nasaimageviewer

import android.content.Context
import androidx.loader.content.AsyncTaskLoader


class QueryLoader(context: Context, private val url: String) :
    AsyncTaskLoader<ArrayList<ImageObj>>(context) {

    // load our value on a background thread
    override fun loadInBackground(): ArrayList<ImageObj> {
        return Query.collectData(url)
    }

    override fun onStartLoading() {
        // if we forget to force load here, we are going to have a bad time. Ask me how I know!
        forceLoad()
    }
}