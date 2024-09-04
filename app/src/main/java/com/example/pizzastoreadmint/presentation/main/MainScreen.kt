package com.example.pizzastoreadmint.presentation.main

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pizzastoreadmint.presentation.funs.dpToPx
import com.example.pizzastoreadmint.navigation.AppNavGraph
import com.example.pizzastoreadmint.navigation.NavigationItem
import com.example.pizzastoreadmint.navigation.Screen
import com.example.pizzastoreadmint.navigation.rememberNavigationState
import com.example.pizzastoreadmint.presentation.city.cities.CitiesScreen
import com.example.pizzastoreadmint.presentation.city.onecity.OneCityScreen
import com.example.pizzastoreadmint.presentation.funs.getScreenWidthDp
import com.example.pizzastoreadmint.presentation.images.images.ImagesScreen
import com.example.pizzastoreadmint.presentation.images.oneimage.OnePictureScreen
import com.example.pizzastoreadmint.presentation.login.LoginScreen
import com.example.pizzastoreadmint.presentation.order.oneorder.OneOrderScreen
import com.example.pizzastoreadmint.presentation.order.orders.OrdersScreen
import com.example.pizzastoreadmint.presentation.product.oneproduct.OneProductScreen
import com.example.pizzastoreadmint.presentation.product.products.ProductsScreen


@Composable
fun MainScreen() {

    CheckNotificationPermissions()

    val navigationState = rememberNavigationState()

    val navBackStackEntry by navigationState.navHostController.currentBackStackEntryAsState()

    val screensWithoutBottom = listOf(
        Screen.ROUTE_LOGIN
    )

    val showBottomBar = navBackStackEntry?.destination?.route !in screensWithoutBottom

    Scaffold(
        bottomBar = {
            if (showBottomBar)
            NavigationBar(
                modifier = Modifier.shadow(elevation = 8.dp)
            ) {

                val items = listOf(
                    NavigationItem.Cities,
                    NavigationItem.Admin,
                    NavigationItem.Orders
                )

                items.forEach { item ->

                    val selected = navBackStackEntry?.destination?.hierarchy?.any {
                        it.route == item.screen.route
                    } ?: false

                    NavigationBarItem(
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
                                    .padding(8.dp)
                                    .size(40.dp),
                                imageVector = ImageVector.vectorResource(item.icon),
                                contentDescription = null
                            )
                        },
                        colors = NavigationBarItemDefaults.colors().copy(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSecondary,
                            selectedIndicatorColor = Color.Transparent
                        )
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
                OnePictureScreen(paddingValues) {
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
            },
            loginScreen = {
                LoginScreen {
                    navigationState.navigateThrowHierarchy(Screen.ROUTE_ORDERS)
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