package com.example.pizzastoreadmint.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun NavGraphBuilder.productScreenNavGraph(
    productsScreenContent: @Composable () -> Unit,
    oneProductScreenContent: @Composable (String?) -> Unit
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
                    nullable = true
                }
            )) {
            val uriEncoded = it.arguments?.getString(Screen.KEY_URI_STRING)
            val uriString = if ((uriEncoded ?: "") != Screen.EMPTY_ARG) {
                uriEncoded
            } else {
                null
            }
            oneProductScreenContent(uriString)
        }
    }
}
