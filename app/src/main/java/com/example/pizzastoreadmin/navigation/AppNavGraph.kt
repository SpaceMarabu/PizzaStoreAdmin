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
    adminScreenContent: @Composable () -> Unit
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.City.route
    ) {
        cityScreenNavGraph(
            citiesScreenContent = citiesScreenContent,
            oneCityScreenContent = oneCityScreenContent
        )
        composable(Screen.Admin.route) {
            adminScreenContent()
        }
//        composable(Screen.Contacts.route) {
//            contactsScreenContent()
//        }
//        composable(Screen.ShoppingBag.route) {
//            shoppingBagScreenContent()
//        }
    }
}
