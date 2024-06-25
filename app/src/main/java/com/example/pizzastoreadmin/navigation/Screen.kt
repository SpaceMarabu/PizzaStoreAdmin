package com.example.pizzastoreadmin.navigation

sealed class Screen(
    val route: String
) {

    object City : Screen(ROUTE_CITY)
    object Cities : Screen(ROUTE_CITIES)
    object OneCity : Screen(ROUTE_ONE_CITY)
    object OneImage : Screen(ROUTE_ONE_IMAGE)
    object Image : Screen(ROUTE_IMAGE)
    object Images : Screen(ROUTE_IMAGES)
    object OneProduct : Screen(ROUTE_ONE_PRODUCT) {

        private const val ROUTE_FOR_ARGS = "one_product"

        fun getRouteWithArgs(uriString: String?): String {
            return "$ROUTE_FOR_ARGS/$uriString"
        }

    }
    object Product : Screen(ROUTE_PRODUCTS)
    object Products : Screen(ROUTE_PRODUCT)
    object Orders : Screen(ROUTE_ORDERS)
    object OneOrder : Screen(ROUTE_ONE_ORDER)
    object Order : Screen(ROUTE_ORDER)


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
    }
}
