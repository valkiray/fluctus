package com.example.fluctus.activities

import android.app.Activity
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.fluctus.R
import com.example.fluctus.databinding.ActivityPlaySongBinding
import com.example.fluctus.utils.SongTimer
import com.example.fluctus.utils.SongsManager
import java.io.File
import java.io.IOException
import java.util.*

class PlaySongActivity : AppCompatActivity(), OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    private lateinit var binding: ActivityPlaySongBinding
    private var isMediaPlayerPrepared = false
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var songManager: SongsManager
    private lateinit var songTimer: SongTimer
    private var handler = Handler(Looper.getMainLooper())
    private var seekForwardTime = 5000
    private var seekBackwardTime = 5000
    private var currentSongIndex = 0
    private var isShuffle = false
    private var isRepeat = false
    private var songList = ArrayList<HashMap<String, String>>()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        setSupportActionBar(binding.toolbar)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.tvJudulLagu.isSelected = true

        // get data intent from adapter
        val intent = intent
        val bundle = intent.extras
        if (bundle != null) {
            currentSongIndex = bundle.getInt("songIndex")
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener(this)
        songManager = SongsManager(this)
        songTimer = SongTimer()

        // get data song
        getPlaySong(currentSongIndex)

        // methods button action
        getButtonSong()

        // updateSeekBar should be called to start updating the seekbar
        updateSeekBar()

        // Enable buttons
        enableButtons(true)
    }

    private fun enableButtons(enable: Boolean) {
        binding.imagePlay.isEnabled = enable
        binding.imageNext.isEnabled = enable
        binding.imagePrev.isEnabled = enable
        binding.imageForward.isEnabled = enable
        binding.imageRewind.isEnabled = enable
        binding.imageRepeat.isEnabled = enable
        binding.imageShuffle.isEnabled = enable
    }

    private fun getButtonSong() {
        binding.imagePlay.setOnClickListener {
            playPause()
        }

        binding.imageNext.setOnClickListener {
            playNextSong()
        }

        binding.imagePrev.setOnClickListener {
            playPreviousSong()
        }

        binding.imageForward.setOnClickListener {
            forwardSong()
        }

        binding.imageRewind.setOnClickListener {
            rewindSong()
        }

        binding.imageRepeat.setOnClickListener {
            toggleRepeat()
        }

        binding.imageShuffle.setOnClickListener {
            toggleShuffle()
        }
    }

    private fun getPlaySong(songIndex: Int) {
        try {
            mediaPlayer.reset()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PlaySongActivity", "Terjadi kesalahan saat me-reset MediaPlayer: ${e.message}")
        }

        try {
            val songPath = songList[songIndex]["songPath"]?.substringBeforeLast(".")
            val songTitle = songList[songIndex]["songTitle"]?.replace("_", " ")

            Log.d("PlaySongActivity", "Song Path (Before Check): $songPath")

            if (songPath.isNullOrEmpty()) {
                Log.e("PlaySongActivity", "Path lagu null atau kosong")
                return
            }

            val file = File(songPath)
            if (!file.exists()) {
                Log.e("PlaySongActivity", "File lagu tidak ditemukan: $songPath")
                return
            }

            Log.d("PlaySongActivity", "Song Path (After Check): $songPath")

            // Set data source asynchronously
            mediaPlayer.setDataSource(songPath)
            mediaPlayer.prepareAsync()

            // Setiap kali pemutaran diulang, kita reset listener terlebih dahulu
            mediaPlayer.setOnPreparedListener(null)
            mediaPlayer.setOnErrorListener(null)

            mediaPlayer.setOnPreparedListener {
                // Persiapan sudah selesai, kita set flag dan lanjutkan pemutaran
                isMediaPlayerPrepared = true

                // Pastikan bahwa MediaPlayer dalam keadaan Prepared sebelum melakukan operasi lain
                if (mediaPlayer.isPlaying || mediaPlayer.duration > 0) {
                    mediaPlayer.start()
                    binding.imagePlay.setImageResource(R.drawable.ic_pause)
                    binding.tvJudulLagu.text = songTitle

                    val totalDuration = mediaPlayer.duration.toLong()
                    binding.tvTotalDuration.text = "" + songTimer.milliSecondsToTimer(totalDuration)

                    // Jalankan pembaruan seekbar setelah persiapan selesai
                    updateSeekBar()
                } else {
                    Log.e("PlaySongActivity", "MediaPlayer not in the correct state for playback")
                }
            }


            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.e("PlaySongActivity", "Error ($what, $extra) while preparing MediaPlayer")
                true
            }

            mediaPlayer.setOnCompletionListener {
                // Handle event completion di sini (jika diperlukan)
            }

            Log.d("PlaySongActivity", "Persiapan MediaPlayer dimulai")

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("PlaySongActivity", "Gagal mempersiapkan MediaPlayer: ${e.message}")
            // Tambahkan pesan ke pengguna jika diperlukan
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PlaySongActivity", "Terjadi kesalahan yang tidak terduga: ${e.message}")
            // Tambahkan pesan ke pengguna jika diperlukan
        }
    }

    private fun playPause() {
        if (!isMediaPlayerPrepared) {
            Log.d("PlaySongActivity", "Waiting for MediaPlayer to be prepared...")
            return
        }

        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            binding.imagePlay.setImageResource(R.drawable.ic_play)
        } else {
            mediaPlayer.start()
            binding.imagePlay.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun playNextSong() {
        if (isMediaPlayerPrepared) {
            try {
                currentSongIndex = if (isRepeat) {
                    currentSongIndex
                } else {
                    (currentSongIndex + 1) % songList.size
                }
                getPlaySong(currentSongIndex)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PlaySongActivity", "Error playing next song: ${e.message}")
            }
        } else {
            Log.d("PlaySongActivity", "Waiting for MediaPlayer to be prepared...")
        }
    }

    private fun playPreviousSong() {
        if (isMediaPlayerPrepared) {
            try {
                currentSongIndex = if (isRepeat) {
                    currentSongIndex
                } else {
                    (currentSongIndex - 1 + songList.size) % songList.size
                }
                getPlaySong(currentSongIndex)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PlaySongActivity", "Error playing previous song: ${e.message}")
            }
        } else {
            Log.d("PlaySongActivity", "Waiting for MediaPlayer to be prepared...")
        }
    }

    private fun forwardSong() {
        if (isMediaPlayerPrepared) {
            val currentPosition = mediaPlayer.currentPosition
            val duration = mediaPlayer.duration
            if (currentPosition + seekForwardTime <= duration) {
                mediaPlayer.seekTo(currentPosition + seekForwardTime)
            } else {
                mediaPlayer.seekTo(duration)
            }
        } else {
            Log.d("PlaySongActivity", "Waiting for MediaPlayer to be prepared...")
        }
    }

    private fun rewindSong() {
        if (isMediaPlayerPrepared) {
            val currentPosition = mediaPlayer.currentPosition
            if (currentPosition - seekBackwardTime >= 0) {
                mediaPlayer.seekTo(currentPosition - seekBackwardTime)
            } else {
                mediaPlayer.seekTo(0)
            }
        } else {
            Log.d("PlaySongActivity", "Waiting for MediaPlayer to be prepared...")
        }
    }

    private fun toggleRepeat() {
        isRepeat = !isRepeat
        updateRepeatButton()

        if (isRepeat) {
            mediaPlayer.isLooping = true
        } else {
            mediaPlayer.isLooping = false
        }
    }

    private fun toggleShuffle() {
        isShuffle = !isShuffle
        updateShuffleButton()

        if (isShuffle) {
            // Implement logic for shuffle mode (if needed)
            // For example, you can shuffle the songList before playing the next song.
            songList.shuffle()
            currentSongIndex = 0
            getPlaySong(currentSongIndex)
        }
    }

    private fun updateRepeatButton() {
        val repeatDrawable =
            if (isRepeat) R.drawable.btn_repeat_focused else R.drawable.btn_repeat
        binding.imageRepeat.setImageResource(repeatDrawable)
        Toast.makeText(
            this@PlaySongActivity,
            if (isRepeat) "Mengulang Tidak Aktif" else "Mengulang Lagu",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateShuffleButton() {
        val shuffleDrawable =
            if (isShuffle) R.drawable.btn_shuffle_focused else R.drawable.btn_shuffle
        binding.imageShuffle.setImageResource(shuffleDrawable)
        Toast.makeText(
            this@PlaySongActivity,
            if (isShuffle) "Acak Lagu, Tidak Aktif" else "Acak Lagu, Aktif",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateSeekBar() {
        handler.postDelayed(runnable, 100)
    }

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (isMediaPlayerPrepared) {
                val totalDuration = mediaPlayer.duration.toLong()
                val currentDuration = mediaPlayer.currentPosition.toLong()
                binding.tvTotalDuration.text = "" + songTimer.milliSecondsToTimer(totalDuration)
                binding.tvCurrentDuration.text = "" + songTimer.milliSecondsToTimer(currentDuration)
                val progress = songTimer.getProgressPercentage(currentDuration, totalDuration)
                binding.seekBar.progress = progress
            }
            handler.postDelayed(this, 100)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        handler.removeCallbacks(runnable)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        handler.removeCallbacks(runnable)
        val totalDuration = mediaPlayer.duration
        val currentPosition = songTimer.progressToTimer(seekBar.progress, totalDuration)
        mediaPlayer.seekTo(currentPosition)

        // run seekbar
        updateSeekBar()
    }

    override fun onCompletion(mp: MediaPlayer) {
        try {
            if (isRepeat) {
                getPlaySong(currentSongIndex)
            } else if (isShuffle) {
                val rand = Random()
                currentSongIndex = rand.nextInt(songList.size)
                getPlaySong(currentSongIndex)
            } else {
                currentSongIndex = (currentSongIndex + 1).coerceAtMost(songList.size - 1)
                getPlaySong(currentSongIndex)
            }

            Log.d("PlaySongActivity", "Pemutaran lagu selesai, lanjutkan ke lagu berikutnya")

            playPause()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PlaySongActivity", "Terjadi kesalahan saat pemutaran lagu berikutnya: ${e.message}")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        handler.removeCallbacks(runnable) // remove callbacks to stop updating seekbar
    }

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }
}
