package com.example.pizzastore.navigation

import android.net.Uri

sealed class Screen(
    val route: String
) {

    object City : Screen(ROUTE_CITY)
    object Cities : Screen(ROUTE_CITIES)
    object OneCity : Screen(ROUTE_ONE_CITY)


    companion object {

        const val ROUTE_CITIES = "cities"
        const val ROUTE_ONE_CITY = "one_city"
        const val ROUTE_CITY = "city"
    }
}
