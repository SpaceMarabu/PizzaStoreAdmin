package com.example.pizzastoreadmin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

class NavigationState(
    val navHostController: NavHostController
) {

    fun navigateTo(route: String) {
        navHostController.navigate(route) {
            popUpTo(Screen.ROUTE_CITIES)
            restoreState = true
            launchSingleTop = true
        }
    }

    fun navigateWithoutPop(route: String) {
        navHostController.navigate(route) {
            restoreState = true
            launchSingleTop = true
        }
    }

    fun navigateToProduct(uriString: String) {
        navHostController.navigate(Screen.OneProduct.getRouteWithArgs(uriString))
    }
}

@Composable
fun rememberNavigationState(
    navHostController: NavHostController = rememberNavController()
): NavigationState {
    return remember {
        NavigationState(navHostController)
    }
}