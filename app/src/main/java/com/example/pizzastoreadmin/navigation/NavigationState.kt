package com.example.pizzastoreadmin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pizzastore.navigation.Screen

class NavigationState(
    val navHostController: NavHostController
) {

    fun navigateTo(route: String) {
        navHostController.navigate(route) {
            popUpTo(Screen.ROUTE_CITY) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateWithDestroy(route: String) {
        navHostController.popBackStack()
        navHostController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
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