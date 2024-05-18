package com.example.pizzastoreadmin.data.firebasedb

import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.Product
import kotlinx.coroutines.flow.Flow

interface FirebaseService {

    fun getListProductsFlow(): Flow<List<Product>>

    suspend fun addOrEditProduct(product: Product): DBResponse

    suspend fun deleteProduct(products: List<Product>): DBResponse

    fun getListCitiesFlow(): Flow<List<City>>

    suspend fun addOrEditCity(city: City): DBResponse

    suspend fun deleteCity(cities: List<City>): DBResponse
}