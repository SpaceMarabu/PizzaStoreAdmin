package com.example.pizzastore.di

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

class PizzaStoreAdminApplication : Application() {

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.factory().create(this)
    }


}

@Composable
fun getApplicationComponent(): ApplicationComponent {
    return (LocalContext.current.applicationContext as PizzaStoreAdminApplication).component
}
