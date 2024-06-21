package com.example.pizzastoreadmin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class NavigationState(
    val navHostController: NavHostController
) {
    fun navigateTo(route: String) {
        navHostController.navigate(route) {
//            popUpTo(Screen.ROUTE_CITIES)
            restoreState = true
            launchSingleTop = true
        }
    }

    fun navigateStartDestination(route: String) {
        navHostController.navigate(route) {
            launchSingleTop = true
            popUpTo(0)
        }
    }

    fun navigateToProduct(uriString: String = Screen.EMPTY_ARG) {
        val encodedUri = if (uriString != Screen.EMPTY_ARG) {
            URLEncoder.encode(uriString, StandardCharsets.UTF_8.toString())
        } else {
            uriString
        }
        navHostController.navigate(
            Screen.OneProduct.getRouteWithArgs(encodedUri)
        ) {
            restoreState = true
            launchSingleTop = true
            popUpTo(0) {
//                saveState = true
//                inclusive = true
            }
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