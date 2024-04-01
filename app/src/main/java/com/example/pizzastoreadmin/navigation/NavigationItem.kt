package com.example.pizzastore.navigation

import com.example.pizzastore.R

sealed class NavigationItem(
    val screen: Screen,
    val titleResId: Int,
    val icon: Int
) {

    object Cities : NavigationItem(
        screen = Screen.Cities,
        titleResId = R.string.cities,
        icon = R.drawable.ic_cross
    )
}
