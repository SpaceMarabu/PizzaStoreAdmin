package com.example.pizzastoreadmin.di

import android.app.Application
import com.example.pizzastoreadmin.di.ApplicationScope
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