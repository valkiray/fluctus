package com.example.fluctus.utils

class SongTimer {
    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        var secondsString = ""
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = ((milliseconds % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        val seconds = ((milliseconds % (1000 * 60 * 60) % (1000 * 60)) / 1000).toInt()
        if (hours > 0) {
            finalTimerString = "$hours:"
        }
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "$seconds"
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"
        return finalTimerString
    }

    fun getProgressPercentage(currentDuration: Long, totalDuration: Long): Int {
        var percentage = 0.0
        val currentSeconds: Long = (currentDuration / 1000)
        val totalSeconds: Long = (totalDuration / 1000)
        if (totalSeconds > 0) {
            percentage = currentSeconds.toDouble() / totalSeconds * 100
        }
        return percentage.toInt()
    }

    fun progressToTimer(progress: Int, totalDuration: Int): Int {
        var currentDuration = 0
        val totalDurationInSeconds = totalDuration / 1000
        if (progress > 0 && totalDurationInSeconds > 0) {
            currentDuration = (progress.toDouble() / 100 * totalDurationInSeconds).toInt()
        }
        return currentDuration * 1000
    }
}
