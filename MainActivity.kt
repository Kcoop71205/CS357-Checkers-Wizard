package com.example.checkerscanvaslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {
    private val backgroundMusic: BackgroundMusic by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start background music
        backgroundMusic.startMusic(R.raw.backgroundmusic)

        setContent {
            AppNavigation(backgroundMusic)
        }
    }
}
