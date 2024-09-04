package com.example.pizzastoreadmint.di

import android.app.Application
import com.example.pizzastoreadmint.data.localdb.PizzaDao
import com.example.pizzastoreadmint.di.ApplicationScope
import com.example.pizzastoreadmint.data.auth.AuthService
import com.example.pizzastoreadmint.data.auth.AuthServiceImpl
import com.example.pizzastoreadmint.data.localdb.LocalDatabase
import com.example.pizzastoreadmint.data.remotedb.AppDatabase
import com.example.pizzastoreadmint.data.remotedb.FirebaseService
import com.example.pizzastoreadmint.data.repository.PizzaStoreRepositoryImpl
import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindRepository(impl: PizzaStoreRepositoryImpl): PizzaStoreRepository

    companion object {

        @ApplicationScope
        @Provides
        fun provideFirebase() : FirebaseService {
            return  AppDatabase()
        }

        @ApplicationScope
        @Provides
        fun providePizzaDao(
            application: Application
        ): PizzaDao {
            return LocalDatabase.getInstance(application).pizzaDao()
        }

        @ApplicationScope
        @Provides
        fun provideAuthService(
            application: Application
        ): AuthService {
            return AuthServiceImpl(application)
        }
    }

}
