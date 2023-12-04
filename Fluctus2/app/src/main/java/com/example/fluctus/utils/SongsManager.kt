package com.example.fluctus.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.fluctus.R
import java.io.File
import java.io.FilenameFilter
import java.util.*

class SongsManager(private val context: Context) {

    private val songsList = ArrayList<HashMap<String, String>>()

    fun getPlayList(): ArrayList<HashMap<String, String>> {
        val resources = context.resources
        val rawResources = resources.obtainTypedArray(R.array.raw_audio_files)
        val judulLaguArray = resources.getStringArray(R.array.judul_lagu)

        for (i in 0 until rawResources.length()) {
            val resourceId = rawResources.getResourceId(i, 0)

            // Check if the resourceId is a valid raw resource ID
            if (resourceId != 0) {
                val songMap = HashMap<String, String>()
                val songTitle = if (i < judulLaguArray.size) judulLaguArray[i] else "Unknown"

                // Construct the correct resource path using the fileName (not resourceId)
                val fileName = resources.getResourceEntryName(resourceId)
                val songPath = "android.resource://${context.packageName}/raw/$fileName"

                songMap["songTitle"] = songTitle
                songMap["songPath"] = songPath

                songsList.add(songMap)

                // Log to check the values
                Log.d("SongsManager", "Song Title: $songTitle, Song Path: $songPath")
            }
        }

        rawResources.recycle()
        return songsList
    }
}