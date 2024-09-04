package com.example.pizzastoreadmint.di

import android.app.Application
import com.example.pizzastoreadmint.di.ApplicationScope
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    @ApplicationScope
    fun provideApplication(application: Application): PizzaStoreAdminApplication {
        return application as PizzaStoreAdminApplication
    }
}