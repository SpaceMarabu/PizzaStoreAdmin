package com.example.pizzastoreadmin.di

import android.app.Application
import com.example.pizzastoreadmin.di.ApplicationScope
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
