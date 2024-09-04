package com.example.pizzastoreadmint.di

import android.app.Application
import com.example.pizzastoreadmint.di.ApplicationScope
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class,
        AppModule::class,
        PresentationModule::class
    ]
)
interface ApplicationComponent {

    fun inject(application: PizzaStoreAdminApplication)

    fun getViewModelFactory(): ViewModelFactory

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}
