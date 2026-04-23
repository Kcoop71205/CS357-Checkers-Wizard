package com.example.checkerscanvaslab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.checkerscanvaslab.ui.theme.CheckersBoardView

@Composable
fun AppNavigation(backgroundMusic: BackgroundMusic) {
    val navController = rememberNavController()
    val volume by backgroundMusic.volume.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onPlayClick = {
                    navController.navigate("play")
                },
                onTutorialClick = {
                    navController.navigate("tutorial")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable("play") {
            AndroidView(
                factory = { context ->
                    CheckersBoardView(context)
                }
            )
        }

        composable("tutorial") {
            TutorialScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                volume = volume,
                onVolumeChange = { backgroundMusic.setVolume(it) },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
