package com.example.pizzastoreadmin.data.repository

import android.net.Uri
import com.example.pizzastoreadmin.data.mappers.RemoteMapper
import com.example.pizzastoreadmin.data.remotedb.FirebaseService
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.Order
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject


class PizzaStoreRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService,
    private val mapper: RemoteMapper
) : PizzaStoreRepository {

    private val currentProduct = MutableStateFlow(Product())
    private val currentCity = MutableStateFlow(City())
    private val currentOrder = MutableStateFlow(Order())

    private val dbResponseFlow: MutableStateFlow<DBResponse> =
        MutableStateFlow(DBResponse.Processing)

    private val typeFlow: MutableStateFlow<PictureType?> = MutableStateFlow(null)
    private val currentPictureUriFlow: MutableStateFlow<Uri?> = MutableStateFlow(null)

    private val listProductsStateFlow = MutableStateFlow<List<Product>>(listOf())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            subscribePicturesTypeFlow()
        }
        CoroutineScope(Dispatchers.IO).launch {
            subscribeListProducts()
        }
    }

    //<editor-fold desc="postPicturesType">
    override suspend fun postPicturesType(type: PictureType) = typeFlow.emit(type)
    //</editor-fold>

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

    //<editor-fold desc="setCurrentProductImageUseCase">
    override fun setCurrentProductImageUseCase(imageUri: Uri?) {
        currentPictureUriFlow.value = imageUri
    }
    //</editor-fold>

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

    //<editor-fold desc="getListImagesUseCase">
    override suspend fun getListImagesUseCase() = firebaseService.getListUriFlow()
    //</editor-fold>

    //<editor-fold desc="getCitiesUseCase">
    override fun getCitiesUseCase(): Flow<List<City>> = firebaseService.getListCitiesFlow()
    //</editor-fold>

    //<editor-fold desc="getProductsUseCase">
    override fun getProductsUseCase(): Flow<List<Product>> =
        firebaseService.getListProductsFlow()
    //</editor-fold>

    //<editor-fold desc="setCurrentCityUseCase">
    override fun setCurrentCityUseCase(city: City?) {
        currentCity.value = city ?: City()
    }
    //</editor-fold>

    //<editor-fold desc="getCurrentCityUseCase">
    override fun getCurrentCityUseCase() = currentCity.asStateFlow()
    //</editor-fold>

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

    //<editor-fold desc="setCurrentProductUseCase">
    override fun setCurrentProductUseCase(product: Product?) {
        currentProduct.value = product ?: Product()
    }
    //</editor-fold>

    //<editor-fold desc="getCurrentProductUseCase">
    override fun getCurrentProductUseCase(): StateFlow<Product> = currentProduct.asStateFlow()
    //</editor-fold>

    //<editor-fold desc="getDbResponse">
    override fun getDbResponse(): StateFlow<DBResponse> {
        dbResponseFlow.value = DBResponse.Processing
        return dbResponseFlow.asStateFlow()
    }
    //</editor-fold>

    //<editor-fold desc="startDbProcessFun">
    private fun startDbProcess(block: suspend CoroutineScope.() -> Unit) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
            block()
        }
    }
    //</editor-fold>

    //<editor-fold desc="subscribeListProducts">
    private suspend fun subscribeListProducts() {
            firebaseService
                .getListProductsFlow()
                .collect {
                    listProductsStateFlow.value = it
                }
    }
    //</editor-fold>

    //<editor-fold desc="getOrdersUseCase">
    override fun getOrdersUseCase(): Flow<List<Order>> =
        firebaseService.getListOrdersFlow()
            .combine(listProductsStateFlow) { ordersDto, products ->
                ordersDto.mapNotNull { orderDto ->
                    mapper.mapOrderDtoToEntity(orderDto, products)
                }
            }
    //</editor-fold>

    //<editor-fold desc="editOrderUseCase">
    override fun editOrderUseCase(order: Order) {
        val orderDto = mapper.mapOrderToOrderDto(order)
        startDbProcess {
            dbResponseFlow.emit(firebaseService.editOrder(orderDto))
        }
    }

    //</editor-fold>

    //<editor-fold desc="setCurrentOrderUseCase">
    override fun setCurrentOrderUseCase(order: Order) {
        currentOrder.value = order
    }
    //</editor-fold>

    //<editor-fold desc="getCurrentOrderUseCase">
    override fun getCurrentOrderUseCase() = currentOrder.asStateFlow()
    //</editor-fold>
}