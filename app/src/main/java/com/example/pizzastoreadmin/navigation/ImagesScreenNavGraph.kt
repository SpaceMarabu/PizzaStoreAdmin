package com.example.pizzastore.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.imageScreenNavGraph(
    imagesScreenContent: @Composable () -> Unit,
    oneImageScreenContent: @Composable () -> Unit
) {
    navigation(
        startDestination = Screen.Images.route,
        route = Screen.Image.route
    ) {
        composable(Screen.Images.route) {
            imagesScreenContent()
        }
        composable(Screen.OneImage.route) {
            oneImageScreenContent()
        }
    }
}
