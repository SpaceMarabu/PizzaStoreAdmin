package com.example.pizzastore.domain.repository

import com.example.pizzastoreadmin.domain.entity.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PizzaStoreRepository {

    fun getCitiesUseCase(): Flow<List<City>>

    fun setCurrentCityUseCase(city: City? = null)

    fun getCurrentCityUseCase(): StateFlow<City?>

    fun addOrEditCityUseCase(city: City)

//    suspend fun editCityUseCase(city: City)

    fun deleteCitiesUseCase(cities: List<City>)
}