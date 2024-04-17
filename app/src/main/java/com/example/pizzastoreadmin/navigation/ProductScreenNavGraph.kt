package com.example.pizzastoreadmin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation

fun NavGraphBuilder.productScreenNavGraph(
    productsScreenContent: @Composable () -> Unit,
    oneProductScreenContent: @Composable (String) -> Unit
) {
    navigation(
        startDestination = Screen.Products.route,
        route = Screen.Product.route
    ) {
        composable(Screen.Products.route) {
            productsScreenContent()
        }
        composable(
            route = Screen.OneProduct.route,
            arguments = listOf(
                navArgument(Screen.KEY_URI_STRING) {
                    type = NavType.StringType
                }
            )) {
            val uriString = it.arguments?.getString(Screen.KEY_URI_STRING) ?: ""
            oneProductScreenContent(uriString)
        }
    }
}
