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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pizzastoreadmin.navigation.AppNavGraph
import com.example.pizzastoreadmin.navigation.NavigationItem
import com.example.pizzastoreadmin.navigation.Screen
import com.example.pizzastoreadmin.navigation.rememberNavigationState
import com.example.pizzastoreadmin.presentation.city.cities.CitiesScreen
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityScreen
import com.example.pizzastoreadmin.presentation.images.images.ImagesScreen
import com.example.pizzastoreadmin.presentation.images.oneimage.OneImageScreen
import com.example.pizzastoreadmin.presentation.order.orders.OrdersScreen
import com.example.pizzastoreadmin.presentation.product.oneproduct.OneProductScreen
import com.example.pizzastoreadmin.presentation.product.products.ProductsScreen


@Composable
fun MainScreen() {

    val navigationState = rememberNavigationState()

    val navBackStackEntry by navigationState.navHostController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            BottomNavigation {

                val items = listOf(
                    NavigationItem.Cities,
                    NavigationItem.Admin,
                    NavigationItem.Orders
                )
//                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->

                    val selected = navBackStackEntry?.destination?.hierarchy?.any {
//                        Log.d("TEST_TEST", navBackStackEntry?.destination?.hierarchy!!.joinToString(" "))
                        Log.d("TEST_TEST", it.route.toString() + " " + item.screen.route)
                        it.route == item.screen.route
                    } ?: false

                    BottomNavigationItem(
//                        selected = currentRoute == item.screen.route,
                        selected = selected,
                        onClick = {
                            if (item.screen.route == Screen.ROUTE_ONE_PRODUCT) {
                                navigationState.navigateToProduct()
                            } else {
                                navigationState.navigateStartDestination(item.screen.route)
                            }
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
            },
            imagesScreenContent = {
                ImagesScreen(
                    paddingValues,
                    addImageClicked = {
                        navigationState.navigateTo(Screen.ROUTE_ONE_IMAGE)
                    }
                ) {
                    navigationState.navigateToProduct(it)
                }
            },
            oneImageScreenContent = {
                OneImageScreen(paddingValues) {
                    navigationState.navigateTo(Screen.ROUTE_IMAGES)
                }
            },
            oneProductScreenContent = {
                OneProductScreen(
                    paddingValues = paddingValues,
                    photoUriString = it,
                    needPhotoUri = {
                        navigationState.navigateTo(Screen.ROUTE_IMAGES)
                    }
                ) {
                    navigationState.navigateTo(Screen.ROUTE_PRODUCTS)
                }
            },
            productsScreenContent = {
                ProductsScreen(paddingValues = paddingValues) {
                    navigationState.navigateTo(Screen.ROUTE_ONE_PRODUCT)
                }
            },
            oneOrderScreenContent = {
                
            },
            ordersScreenContent = {
                OrdersScreen(paddingValues = paddingValues) {
                    
                }
            }
        )
    }
}

