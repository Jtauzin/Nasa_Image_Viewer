package com.bronzeswordstudios.nasaimageviewer

class ImageObj(
        // our ImageObj class holds our information for each image to be displayed
        private val url: String?,
        private val title: String?,
        private val center: String?,
        private val date: String?,
        private val backupURL: String?
) {


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
}