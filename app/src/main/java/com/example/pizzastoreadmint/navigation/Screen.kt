package com.example.pizzastoreadmint.navigation

sealed class Screen(
    val route: String
) {

    data object City : Screen(ROUTE_CITY)
    data object Cities : Screen(ROUTE_CITIES)
    data object OneCity : Screen(ROUTE_ONE_CITY)
    data object OneImage : Screen(ROUTE_ONE_IMAGE)
    data object Image : Screen(ROUTE_IMAGE)
    data object Images : Screen(ROUTE_IMAGES)
    data object OneProduct : Screen(ROUTE_ONE_PRODUCT) {

        private const val ROUTE_FOR_ARGS = "one_product"

        fun getRouteWithArgs(uriString: String?): String {
            return "$ROUTE_FOR_ARGS/$uriString"
        }

    }
    data object Product : Screen(ROUTE_PRODUCTS)
    data object Products : Screen(ROUTE_PRODUCT)
    data object Orders : Screen(ROUTE_ORDERS)
    data object OneOrder : Screen(ROUTE_ONE_ORDER)
    data object Order : Screen(ROUTE_ORDER)
    data object Login : Screen(ROUTE_LOGIN)


    companion object {

        const val KEY_URI_STRING = "uriString"

        const val EMPTY_ARG = "empty_arg"

        const val ROUTE_CITIES = "cities"
        const val ROUTE_ONE_CITY = "one_city"
        const val ROUTE_CITY = "city"

        const val ROUTE_ONE_IMAGE = "one_image"
        const val ROUTE_IMAGES = "images"
        const val ROUTE_IMAGE = "image"

        const val ROUTE_ONE_PRODUCT = "one_product/{$KEY_URI_STRING}"
        const val ROUTE_PRODUCTS = "products"
        const val ROUTE_PRODUCT = "product"

        const val ROUTE_ORDERS = "orders"
        const val ROUTE_ONE_ORDER = "one_order"
        const val ROUTE_ORDER = "order"

        const val ROUTE_LOGIN = "login"
    }
}
