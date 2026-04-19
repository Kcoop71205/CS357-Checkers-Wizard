package com.example.checkerscanvaslab

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.checkerscanvaslab.ui.theme.CheckersBoardView

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onPlayClick = {
                    navController.navigate("play")
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
    }
}
