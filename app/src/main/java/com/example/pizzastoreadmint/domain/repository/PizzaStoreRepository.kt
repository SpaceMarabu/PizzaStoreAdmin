package com.example.pizzastoreadmint.domain.repository

import android.net.Uri
import com.example.pizzastoreadmint.domain.entity.Order
import com.example.pizzastoreadmint.data.repository.states.DBResponse
import com.example.pizzastoreadmint.domain.entity.City
import com.example.pizzastoreadmint.domain.entity.PictureType
import com.example.pizzastoreadmint.domain.entity.Product
import com.example.pizzastoreadmint.domain.entity.SignInFailEvents
import com.example.pizzastoreadmint.domain.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PizzaStoreRepository {

    fun getCitiesUseCase(): Flow<List<City>>

    fun addOrEditCityUseCase(city: City)

    fun deleteCitiesUseCase(cities: List<City>)

    suspend fun getListPicturesUseCase(): SharedFlow<List<Uri>>

    fun putImageToStorageUseCase(name: String, type: String, imageByte: ByteArray)

    fun deleteImagesUseCase(listToDelete: List<Uri>)

    fun addOrEditProductUseCase(product: Product)

    fun getProductsUseCase(): Flow<List<Product>>

    fun deleteProductsUseCase(products: List<Product>)

    suspend fun getOrdersUseCase(): Flow<List<Order>>

    fun editOrderUseCase(order: Order)

    fun getUserUseCase(): StateFlow<User?>

    suspend fun signInWithEmail(email: String, password: String)

    suspend fun signInWithSavedAccounts()

    suspend fun createUserWithEmail(email: String, password: String)

    suspend fun signOut()


    //<editor-fold desc="service UC">
    fun getDbResponse(): StateFlow<DBResponse>

    fun getCurrentCityUseCase(): StateFlow<City>

    fun setCurrentCityUseCase(city: City? = null)

    fun setCurrentProductImageUseCase(imageUri: Uri? = null)

    suspend fun postPicturesType(type: PictureType)

    fun setCurrentProductUseCase(product: Product? = null)

    fun getCurrentProductUseCase(): StateFlow<Product>

    fun setCurrentOrderUseCase(order: Order)

    fun getCurrentOrderUseCase(): StateFlow<Order>

    val signInEvents: SharedFlow<SignInFailEvents>
    //</editor-fold>

}