package com.example.pizzastore.di

import android.app.Application
import com.example.pizzastore.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.data.repository.PizzaStoreRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindRepository(impl: PizzaStoreRepositoryImpl): PizzaStoreRepository

}
