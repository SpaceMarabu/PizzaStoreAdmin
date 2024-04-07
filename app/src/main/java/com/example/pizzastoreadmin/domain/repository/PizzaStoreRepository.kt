package com.example.pizzastoreadmin.domain.repository

import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PizzaStoreRepository {

    fun getCitiesUseCase(): Flow<List<City>>

    fun addOrEditCityUseCase(city: City)

    fun deleteCitiesUseCase(cities: List<City>)

    //<editor-fold desc="service UC">
    fun getDbResponse(): StateFlow<DBResponse>

    fun getCurrentCityUseCase(): StateFlow<City>

    fun setCurrentCityUseCase(city: City? = null)

    fun putImageToStorage(name: String, imageByte: ByteArray)
    //</editor-fold>

}