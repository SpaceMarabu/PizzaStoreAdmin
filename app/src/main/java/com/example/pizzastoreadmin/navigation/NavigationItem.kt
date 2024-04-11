package com.example.pizzastoreadmin.navigation

import com.example.pizzastore.R
import com.example.pizzastore.navigation.Screen

sealed class NavigationItem(
    val screen: Screen,
    val titleResId: Int,
    val icon: Int
) {

    object Cities : NavigationItem(
        screen = Screen.Cities,
        titleResId = R.string.cities,
        icon = R.drawable.ic_city
    )

    object Admin : NavigationItem(
        screen = Screen.OneImage,
        titleResId = R.string.admin,
        icon = R.drawable.ic_admin
    )
}
