package com.fglabs.app.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File

class AudioPlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    fun playFile(file: File) {
        stop() // Stop any current playback
        MediaPlayer.create(context, file.toUri()).apply {
            player = this
            start()
            setOnCompletionListener {
                stop()
            }
        }
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}
