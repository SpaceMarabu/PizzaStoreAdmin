package com.example.pizzastore.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(
    navHostController: NavHostController,
    citiesScreenContent: @Composable () -> Unit,
    oneCityScreenContent: @Composable () -> Unit,
    oneImageScreenContent: @Composable () -> Unit,
    imagesScreenContent: @Composable () -> Unit
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.City.route
    ) {
        cityScreenNavGraph(
            citiesScreenContent = citiesScreenContent,
            oneCityScreenContent = oneCityScreenContent
        )
        imageScreenNavGraph(
            imagesScreenContent = imagesScreenContent,
            oneImageScreenContent = oneImageScreenContent
        )
    }
}
