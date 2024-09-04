package com.example.pizzastoreadmint.navigation

import com.example.pizzastoreadmint.R

sealed class NavigationItem(
    val screen: Screen,
    val titleResId: Int,
    val icon: Int
) {

    object Cities : NavigationItem(
        screen = Screen.City,
        titleResId = R.string.cities,
        icon = R.drawable.ic_city
    )

    object Admin : NavigationItem(
        screen = Screen.Product,
        titleResId = R.string.admin,
        icon = R.drawable.ic_admin
    )

    object Orders : NavigationItem(
        screen = Screen.Order,
        titleResId = R.string.order,
        icon = R.drawable.ic_order
    )
}
