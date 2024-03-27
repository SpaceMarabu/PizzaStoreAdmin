package com.example.pizzastore.domain.repository

import com.example.pizzastoreadmin.domain.entity.City
import kotlinx.coroutines.flow.Flow

interface PizzaStoreRepository {

    fun getCitiesUseCase(): Flow<List<City>>

    fun addOrEditCityUseCase(city: City)

//    suspend fun editCityUseCase(city: City)

    fun deleteCitiesUseCase(cities: List<City>)
}