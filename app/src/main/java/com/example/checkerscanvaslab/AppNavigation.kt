package com.example.checkerscanvaslab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
            val palette by GameSettings.palette.collectAsState()
            AndroidView(
                factory = { context ->
                    CheckersBoardView(context).apply {
                        onHomeClick = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                        onVictory = {
                            navController.navigate("victory") {
                                popUpTo("victory") { inclusive = true }
                            }
                        }
                        onDefeat = {
                            navController.navigate("defeat") {
                                popUpTo("defeat") { inclusive = true }
                            }
                        }

                    }
                },
                update = { view -> view.setColors(palette)}
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
        composable("victory") {
            VictoryScreen(
                onHomeClick = {
                    navController.navigate("home")
                },
                onPlayClick = {
                    navController.navigate("play")
                }
            )
        }
        composable("defeat") {
            DefeatScreen(
                onHomeClick = {
                    navController.navigate("home")
                },
                onPlayClick = {
                    navController.navigate("play")
                }
            )
        }


    }
}
