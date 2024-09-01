package com.example.pizzastoreadmin.di

import android.app.Application
import com.example.pizzastoreadmin.data.localdb.PizzaDao
import com.example.pizzastore.di.ApplicationScope
import com.example.pizzastoreadmin.data.auth.AuthService
import com.example.pizzastoreadmin.data.auth.AuthServiceImpl
import com.example.pizzastoreadmin.data.localdb.LocalDatabase
import com.example.pizzastoreadmin.data.remotedb.AppDatabase
import com.example.pizzastoreadmin.data.remotedb.FirebaseService
import com.example.pizzastoreadmin.data.repository.PizzaStoreRepositoryImpl
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
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
