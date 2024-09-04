package com.example.pizzastoreadmint.data.remotedb

import android.net.Uri
import com.example.pizzastoreadmint.data.remotedb.entity.OrderDto
import com.example.pizzastoreadmint.data.remotedb.entity.UserDto
import com.example.pizzastoreadmint.data.repository.states.DBResponse
import com.example.pizzastoreadmint.domain.entity.City
import com.example.pizzastoreadmint.domain.entity.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface FirebaseService {

    fun getListProductsFlow(): Flow<List<Product>>

    suspend fun addOrEditProduct(product: Product): DBResponse

    suspend fun deleteProduct(products: List<Product>): DBResponse

    fun getListCitiesFlow(): Flow<List<City>>

    suspend fun addOrEditCity(city: City): DBResponse

    suspend fun deleteCity(cities: List<City>): DBResponse

    suspend fun putImageToStorage(name: String, type: String, imageByte: ByteArray): DBResponse

    suspend fun deletePictures(listToDelete: List<Uri>): Boolean

    fun getListUriFlow(): SharedFlow<List<Uri>>

    suspend fun loadPictures(type: String)

    fun getListOrdersFlow(): Flow<List<OrderDto>>

    suspend fun getListOrdersOneTime(): List<OrderDto>

    suspend fun editOrder(orderDto: OrderDto): DBResponse

    suspend fun getUsers(): List<UserDto>

    suspend fun addUser(userDto: UserDto): DBResponse
}