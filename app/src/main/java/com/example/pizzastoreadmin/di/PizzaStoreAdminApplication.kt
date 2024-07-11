package com.example.pizzastoreadmin.di

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.work.Configuration
import com.example.pizzastore.di.ApplicationComponent
import com.example.pizzastore.di.DaggerApplicationComponent
import com.example.pizzastoreadmin.data.mappers.RemoteMapper
import com.example.pizzastoreadmin.data.remotedb.FirebaseService
import com.example.pizzastoreadmin.data.workers.RefreshDataWorkerFactory
import javax.inject.Inject

class PizzaStoreAdminApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: RefreshDataWorkerFactory

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        component.inject(this)
        super.onCreate()
    }
}

@Composable
fun getApplicationComponent(): ApplicationComponent {
    return (LocalContext.current.applicationContext as PizzaStoreAdminApplication).component
}
