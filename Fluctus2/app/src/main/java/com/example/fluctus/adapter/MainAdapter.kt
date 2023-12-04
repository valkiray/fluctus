package com.example.fluctus.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fluctus.R
import com.example.fluctus.activities.PlaySongActivity
import com.example.fluctus.databinding.ListItemMainBinding
import java.io.IOException
import java.util.Locale

class MainAdapter(private val songList: MutableList<HashMap<String, String>>, private val context: Context) :
    RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ListItemMainBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: HashMap<String, String>, context: Context) {
            binding.tvJudulLagu.text = song["songTitle"]

            binding.root.setOnClickListener {
                playLagu(song, context)
            }
        }

        @SuppressLint("DiscouragedApi")
        private fun playLagu(song: HashMap<String, String>, context: Context) {
            try {
                val fileName = song["songPath"] ?: return
                val uri = Uri.parse(fileName)

                Log.d("MediaPlayer", "Playing URI: $uri")

                val mediaPlayer = MediaPlayer()

                mediaPlayer.setDataSource(context, uri)
                mediaPlayer.prepareAsync()

                mediaPlayer.setOnPreparedListener { mp ->
                    mp.start()
                    navigateToPlaySongActivity(context)  // Start playback and navigate to PlaySongActivity
                }

                mediaPlayer.setOnCompletionListener {
                    mediaPlayer.release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal memutar lagu", Toast.LENGTH_SHORT).show()
            }
        }

        private fun navigateToPlaySongActivity(context: Context) {
            val intent = Intent(context, PlaySongActivity::class.java)
            context.startActivity(intent)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songList[position]
        holder.bind(song, context)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    fun updateData(newData: List<HashMap<String, String>>) {
        songList.clear()
        songList.addAll(newData)
        notifyDataSetChanged()
    }
}