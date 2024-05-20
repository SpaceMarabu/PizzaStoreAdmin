package com.example.pizzastoreadmin.data.repository

import android.net.Uri
import com.example.pizzastoreadmin.data.firebasedb.FirebaseService
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class PizzaStoreRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : PizzaStoreRepository {

    private val currentProduct: MutableStateFlow<Product> = MutableStateFlow(Product())
    private val currentCity: MutableStateFlow<City> = MutableStateFlow(City())

    private val dbResponseFlow: MutableStateFlow<DBResponse> =
        MutableStateFlow(DBResponse.Processing)

    private val typeFlow: MutableStateFlow<PictureType?> = MutableStateFlow(null)
    private val currentPictureUriFlow: MutableStateFlow<Uri?> = MutableStateFlow(null)


    init {
        CoroutineScope(Dispatchers.IO).launch {
            subscribePicturesTypeFlow()
        }
    }

    override suspend fun postPicturesType(type: PictureType) = typeFlow.emit(type)

    //<editor-fold desc="deletePicturesUseCase">
    override fun deleteImagesUseCase(listToDelete: List<Uri>) {

        val deferred = CompletableDeferred(true)
        CoroutineScope(Dispatchers.IO).launch {
            deferred.complete(
                firebaseService.deletePictures(listToDelete)
            )
            deferred.await()
        }

        val currentType = typeFlow.value?.type
        CoroutineScope(Dispatchers.IO).launch {
            if (currentType != null) {
                firebaseService.loadPictures(currentType)
            }
        }
    }
    //</editor-fold>

    override fun setCurrentProductImageUseCase(imageUri: Uri?) {
        currentPictureUriFlow.value = imageUri
    }

    //<editor-fold desc="subscribeTypeFlow">
    private suspend fun subscribePicturesTypeFlow() {
        typeFlow.collect {
            if (it != null) {
                firebaseService.loadPictures(it.type)
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="putImageToStorage">
    override fun putImageToStorageUseCase(name: String, type: String, imageByte: ByteArray) {
        startDbProcess {
            dbResponseFlow.emit(
                firebaseService.putImageToStorage(name, type, imageByte)
            )
        }
    }
//</editor-fold>

    override suspend fun getListImagesUseCase() = firebaseService.getListUriFlow()

    override fun getCitiesUseCase(): Flow<List<City>> = firebaseService.getListCitiesFlow()

    override fun getProductsUseCase(): Flow<List<Product>> =
        firebaseService.getListProductsFlow()

    override fun setCurrentCityUseCase(city: City?) {
        currentCity.value = city ?: City()
    }

    override fun getCurrentCityUseCase() = currentCity.asStateFlow()

    //<editor-fold desc="addOrEditCityUseCase">
    override fun addOrEditCityUseCase(city: City) {
        startDbProcess {
            dbResponseFlow.emit(
                firebaseService.addOrEditCity(city)
            )
        }
    }
    //</editor-fold>

    //<editor-fold desc="deleteCitiesUseCase">
    override fun deleteCitiesUseCase(cities: List<City>) {
        startDbProcess {
            dbResponseFlow.emit(
                firebaseService.deleteCity(cities)
            )
        }
    }
    //</editor-fold>

    //<editor-fold desc="addOrEditProductUseCase">
    override fun addOrEditProductUseCase(product: Product) {
        startDbProcess {
            dbResponseFlow.emit(
                firebaseService.addOrEditProduct(product)
            )
        }
    }
    //</editor-fold>

    //<editor-fold desc="deleteProductsUseCase">
    override fun deleteProductsUseCase(products: List<Product>) {
        startDbProcess {
            dbResponseFlow.emit(firebaseService.deleteProduct(products))
        }
    }
    //</editor-fold>

    override fun setCurrentProductUseCase(product: Product?) {
        currentProduct.value = product ?: Product()
    }

    override fun getCurrentProductUseCase(): StateFlow<Product> = currentProduct.asStateFlow()

    override fun getDbResponse(): StateFlow<DBResponse> {
        dbResponseFlow.value = DBResponse.Processing
        return dbResponseFlow.asStateFlow()
    }

    //<editor-fold desc="startDbProcessFun">
    private fun startDbProcess(block: suspend CoroutineScope.() -> Unit) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
            block()
        }
    }
    //</editor-fold>
}