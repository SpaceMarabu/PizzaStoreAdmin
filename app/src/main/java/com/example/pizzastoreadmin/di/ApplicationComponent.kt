package com.example.pizzastore.di

import android.app.Application
import com.example.pizzastoreadmin.di.ViewModelModule
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent {

    fun getViewModelFactory(): ViewModelFactory

    fun inject(application: PizzaStoreAdminApplication)


    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
//            @BindsInstance context: Context
        ): ApplicationComponent
    }
}
