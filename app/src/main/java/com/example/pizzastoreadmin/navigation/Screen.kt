package com.example.pizzastore.navigation

sealed class Screen(
    val route: String
) {

    object City : Screen(ROUTE_CITY)
    object Cities : Screen(ROUTE_CITIES)
    object OneCity : Screen(ROUTE_ONE_CITY)
    object OneImage : Screen(ROUTE_ONE_IMAGE)
    object Image : Screen(ROUTE_IMAGE)
    object Images : Screen(ROUTE_IMAGES)


    companion object {

        const val ROUTE_CITIES = "cities"
        const val ROUTE_ONE_CITY = "one_city"
        const val ROUTE_CITY = "city"
        const val ROUTE_ONE_IMAGE = "one_image"
        const val ROUTE_IMAGES = "images"
        const val ROUTE_IMAGE = "image"
    }
}
