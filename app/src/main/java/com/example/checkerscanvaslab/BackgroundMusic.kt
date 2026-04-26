package com.example.checkerscanvaslab

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BackgroundMusic(application: Application) : AndroidViewModel(application) {
    private var mediaPlayer: MediaPlayer? = null

    private val _volume = MutableStateFlow(0.5f)
    val volume: StateFlow<Float> = _volume

    fun startMusic(resId: Int) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplication(), resId).apply {
                isLooping = true
                setVolume(_volume.value, _volume.value)
                start()
            }
        }
    }

    fun setVolume(value: Float) {
        _volume.value = value
        mediaPlayer?.setVolume(value, value)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}