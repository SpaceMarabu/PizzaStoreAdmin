package com.example.pizzastore.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.cityScreenNavGraph(
    citiesScreenContent: @Composable () -> Unit,
    oneCityScreenContent: @Composable () -> Unit
) {
    navigation(
        startDestination = Screen.Cities.route,
        route = Screen.Cities.route
    ) {
        composable(Screen.Cities.route) {
            citiesScreenContent()
        }
        composable(Screen.OneCity.route) {
            oneCityScreenContent()
        }
    }
}
