// Nate's attempt to create a class to store game settings so this information will
// be consistent across the whole project, similar to what Grace did with the music player
// Does not work

package com.example.checkerscanvaslab

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object GameSettings {

    private val _palette = MutableStateFlow("original")
    val palette: StateFlow<String> = _palette

    fun setPalette(value: String) {
        _palette.value = value
    }
}