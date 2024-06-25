package com.example.pizzastoreadmin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.orderScreenNavGraph(
    ordersScreenContent: @Composable () -> Unit,
    oneOrderScreenContent: @Composable () -> Unit
) {
    navigation(
        startDestination = Screen.Orders.route,
        route = Screen.Order.route
    ) {
        composable(Screen.Orders.route) {
            ordersScreenContent()
        }
        composable(Screen.OneOrder.route) {
            oneOrderScreenContent()
        }
    }
}
