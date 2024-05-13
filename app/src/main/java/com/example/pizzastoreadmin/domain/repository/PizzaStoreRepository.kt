package com.example.pizzastoreadmin.domain.repository

import android.net.Uri
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.entity.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PizzaStoreRepository {

    fun getCitiesUseCase(): Flow<List<City>>

    fun addOrEditCityUseCase(city: City)

    fun deleteCitiesUseCase(cities: List<City>)

    suspend fun getListImagesUseCase(): SharedFlow<List<Uri>>

    fun putImageToStorage(name: String, type: String, imageByte: ByteArray)

    fun deleteImagesUseCase(listToDelete: List<Uri>)

    fun addOrEditProductUseCase(product: Product)

    fun getProductsUseCase(): Flow<List<Product>>

    fun deleteProductsUseCase(products: List<Product>)


    //<editor-fold desc="service UC">
    fun getDbResponse(): StateFlow<DBResponse>

    fun getCurrentCityUseCase(): StateFlow<City>

    fun setCurrentCityUseCase(city: City? = null)

    fun setCurrentProductImageUseCase(imageUri: Uri? = null)

    suspend fun postPicturesType(type: PictureType)

    fun setCurrentProductUseCase(product: Product? = null)

    fun getCurrentProductUseCase(): StateFlow<Product>
    //</editor-fold>

}