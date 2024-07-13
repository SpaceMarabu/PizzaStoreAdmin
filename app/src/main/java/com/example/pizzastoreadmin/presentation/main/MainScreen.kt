package com.example.pizzastoreadmin.presentation.main

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pizzastoreadmin.navigation.AppNavGraph
import com.example.pizzastoreadmin.navigation.NavigationItem
import com.example.pizzastoreadmin.navigation.Screen
import com.example.pizzastoreadmin.navigation.rememberNavigationState
import com.example.pizzastoreadmin.presentation.city.cities.CitiesScreen
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityScreen
import com.example.pizzastoreadmin.presentation.images.images.ImagesScreen
import com.example.pizzastoreadmin.presentation.images.oneimage.OneImageScreen
import com.example.pizzastoreadmin.presentation.order.oneorder.OneOrderScreen
import com.example.pizzastoreadmin.presentation.order.orders.OrdersScreen
import com.example.pizzastoreadmin.presentation.product.oneproduct.OneProductScreen
import com.example.pizzastoreadmin.presentation.product.products.ProductsScreen


@Composable
fun MainScreen() {

    CheckNotificationPermissions()

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

                items.forEach { item ->

                    val selected = navBackStackEntry?.destination?.hierarchy?.any {
                        Log.d("BackStackLog", "BackStack: ${it.route} == ${item.screen.route}")
                        it.route == item.screen.route
                    } ?: false

                    BottomNavigationItem(
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
                    navigationState.navigateThrowHierarchy(Screen.ROUTE_ONE_CITY)
                }
            },
            oneCityScreenContent = {
                OneCityScreen(paddingValues) {
                    navigationState.navigateThrowHierarchy(Screen.ROUTE_CITY)
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
                OneOrderScreen(paddingValues = paddingValues) {
                    navigationState.navigateThrowHierarchy(Screen.ROUTE_ORDERS)
                }
            },
            ordersScreenContent = {
                OrdersScreen(paddingValues = paddingValues) {
                    navigationState.navigateThrowHierarchy(Screen.ROUTE_ONE_ORDER)
                }
            }
        )
    }
}

//<editor-fold desc="CheckPermissionForTiramisu">
@Composable
fun CheckNotificationPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        var permissionGranted by remember {
            mutableStateOf(false)
        }

        val permissions = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS
        )

        val permissionLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissionsMap ->
                permissionGranted = permissionsMap.values.all { it }
                        && permissionsMap.values.isNotEmpty()
            }

        if (!permissionGranted) {
            SideEffect {
                permissionLauncher.launch(permissions)
            }
        }
    }
}
//</editor-fold>