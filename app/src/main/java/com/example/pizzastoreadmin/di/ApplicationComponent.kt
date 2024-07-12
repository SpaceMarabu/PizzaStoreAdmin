package com.example.pizzastoreadmin.di

import android.app.Application
import com.example.pizzastore.di.ApplicationScope
import com.example.pizzastore.di.ViewModelFactory
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class,
        AppModule::class
    ]
)
interface ApplicationComponent {

    fun getViewModelFactory(): ViewModelFactory

    fun inject(application: PizzaStoreAdminApplication)


    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}
