package com.example.pizzastoreadmin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.pizzastore.navigation.cityScreenNavGraph
import com.example.pizzastore.navigation.imageScreenNavGraph

@Composable
fun AppNavGraph(
    navHostController: NavHostController,
    citiesScreenContent: @Composable () -> Unit,
    oneCityScreenContent: @Composable () -> Unit,
    oneImageScreenContent: @Composable () -> Unit,
    imagesScreenContent: @Composable () -> Unit,
    oneProductScreenContent: @Composable (String) -> Unit,
    productsScreenContent: @Composable () -> Unit
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
        productScreenNavGraph(
            productsScreenContent = productsScreenContent,
            oneProductScreenContent = oneProductScreenContent
        )
    }
}
