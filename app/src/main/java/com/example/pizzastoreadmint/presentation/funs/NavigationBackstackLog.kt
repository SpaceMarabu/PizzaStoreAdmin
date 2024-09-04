package com.example.pizzastoreadmint.presentation.funs

import android.util.Log
import androidx.compose.runtime.Composable
import com.example.pizzastoreadmint.navigation.rememberNavigationState

@Composable
fun NavigationBackstackLog() {
    val navigationState = rememberNavigationState()
    navigationState.navHostController
        .addOnDestinationChangedListener { controller, _, _ ->
            val routes = controller
                .backQueue
                .map { it.destination.route }
                .joinToString(", ")

            Log.d("BackStackLog", "BackStack: $routes")
        }
}