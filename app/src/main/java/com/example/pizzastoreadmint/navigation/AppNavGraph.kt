package com.example.pizzastoreadmint.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pizzastoreadmint.navigation.cityScreenNavGraph
import com.example.pizzastoreadmint.navigation.imageScreenNavGraph

@Composable
fun AppNavGraph(
    navHostController: NavHostController,
    citiesScreenContent: @Composable () -> Unit,
    oneCityScreenContent: @Composable () -> Unit,
    oneImageScreenContent: @Composable () -> Unit,
    imagesScreenContent: @Composable () -> Unit,
    oneProductScreenContent: @Composable (String?) -> Unit,
    productsScreenContent: @Composable () -> Unit,
    oneOrderScreenContent: @Composable () -> Unit,
    ordersScreenContent: @Composable () -> Unit,
    loginScreen: @Composable () -> Unit
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.Login.route
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
        orderScreenNavGraph(
            ordersScreenContent = ordersScreenContent,
            oneOrderScreenContent = oneOrderScreenContent
        )
        composable(Screen.Login.route) {
            loginScreen()
        }
    }
}
