package com.example.fluctus.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fluctus.adapter.MainAdapter
import com.example.fluctus.databinding.ActivityMainBinding
import com.example.fluctus.utils.SongsManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var songsAdapter: MainAdapter
    private lateinit var songsManager: SongsManager

    private var songList = ArrayList<HashMap<String, String>>()

    private val permissionArrays = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var someActivityResultLauncher: ActivityResultLauncher<Intent>

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestPermissions()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
        if (Build.VERSION.SDK_INT >= 21) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
            window.statusBarColor = Color.TRANSPARENT
        }

        val searchPlateId =
            binding.svLagu.context.resources.getIdentifier(
                "android:id/search_plate",
                null,
                null
            )
        val searchPlate = binding.svLagu.findViewById<View>(searchPlateId)
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)

        songsManager = SongsManager(this)
        songList = songsManager.getPlayList()
        songsAdapter = MainAdapter(songList, this)
        binding.rvListMusic.setHasFixedSize(true)
        binding.rvListMusic.layoutManager = LinearLayoutManager(this)
        binding.rvListMusic.adapter = songsAdapter

        someActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    data?.data?.let { uri ->
                        handleSelectedAudio(uri)
                    }
                }
            }

    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        for (permission in permissionArrays) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            var anyPermissionDenied = false

            for (grantResult in grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    anyPermissionDenied = true
                    // You can show a message or take other actions for denied permission
                    break
                }
            }

            if (anyPermissionDenied) {
                finish()
            } else {
            }
        }
    }

    private val PICK_AUDIO_REQUEST_CODE = 123

    private fun pickAudioFile(someActivityResultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        someActivityResultLauncher.launch(intent)
    }

    private fun handleSelectedAudio(uri: Uri) {
        val sourceFile = File(getPathFromUri(uri))
        val destinationDir = File(getExternalFilesDir(null), "fluctus")

        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }

        val destFile = File(destinationDir, sourceFile.nameWithoutExtension) // Menghapus ekstensi file

        try {
            copyFile(sourceFile, destFile)
            // Update your song list or perform any necessary actions here
            songList = songsManager.getPlayList()
            songsAdapter.updateData(songList)
            Toast.makeText(this, "Song added successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to copy file", Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("NewApi")
    private fun getPathFromUri(uri: Uri): String {
        var path = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(cursor.getColumnIndexOrThrow("_data"))
                    }
                } finally {
                    cursor.close()
                }
            }
        } else {
            path = uri.path ?: ""
        }
        return path
    }

    private fun copyFile(sourceFile: File, destFile: File) {
        var source: FileChannel? = null
        var destination: FileChannel? = null
        try {
            source = FileInputStream(sourceFile).channel
            destination = FileOutputStream(destFile).channel
            destination.transferFrom(source, 0, source.size())
        } finally {
            source?.close()
            destination?.close()
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 101
    }
}
