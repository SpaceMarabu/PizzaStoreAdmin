package com.example.pizzastoreadmin.di

import com.example.pizzastore.di.ApplicationScope
import com.example.pizzastoreadmin.data.firebasedb.AppDatabase
import com.example.pizzastoreadmin.data.firebasedb.FirebaseService
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.data.repository.PizzaStoreRepositoryImpl
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
    }

}
