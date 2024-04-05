package com.example.pizzastoreadmin.presentation.main

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pizzastore.navigation.AppNavGraph
import com.example.pizzastore.navigation.NavigationItem
import com.example.pizzastore.navigation.Screen
import com.example.pizzastoreadmin.navigation.rememberNavigationState
import com.example.pizzastoreadmin.presentation.city.cities.CitiesScreen
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityScreen


@Composable
fun MainScreen() {


    val navigationState = rememberNavigationState()

    val navBackStackEntry by navigationState.navHostController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            BottomNavigation {

                val items = listOf(
                    NavigationItem.Cities,
                    NavigationItem.Cities,
                    NavigationItem.Cities
                )
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->

                    BottomNavigationItem(
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            navigationState.navigateTo(item.screen.route)
                        },
                        icon = {
                            Icon(
                                modifier = Modifier
                                    .padding(8.dp),
                                imageVector = ImageVector.vectorResource(item.icon),
                                contentDescription = null
                            )
                        },
                        selectedContentColor = MaterialTheme.colors.onPrimary,
                        unselectedContentColor = MaterialTheme.colors.onSecondary
                    )
                }
            }

        }
    ) { paddingValues ->

        AppNavGraph(
            navHostController = navigationState.navHostController,
            citiesScreenContent = {
                CitiesScreen(paddingValues) {
                    navigationState.navigateTo(Screen.ROUTE_ONE_CITY)
                }
            },
            oneCityScreenContent = {
                OneCityScreen(paddingValues) {
                    navigationState.navigateTo(Screen.ROUTE_CITY)
                }
            }
        )
    }
}

