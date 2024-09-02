package com.example.pizzastoreadmin.data.repository

import android.net.Uri
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.pizzastoreadmin.data.auth.AuthResponse
import com.example.pizzastoreadmin.data.auth.AuthService
import com.example.pizzastoreadmin.data.localdb.PizzaDao
import com.example.pizzastoreadmin.data.localdb.entity.orders.OrderDbModel
import com.example.pizzastoreadmin.data.localdb.entity.products.ProductDbModel
import com.example.pizzastoreadmin.data.mappers.LocalMapper
import com.example.pizzastoreadmin.data.mappers.RemoteMapper
import com.example.pizzastoreadmin.data.remotedb.FirebaseService
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.data.workers.LoadOrdersWorker
import com.example.pizzastoreadmin.di.PizzaStoreAdminApplication
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.entity.SignInEvents
import com.example.pizzastoreadmin.domain.entity.User
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


class PizzaStoreRepositoryImpl @Inject constructor(
    private val application: PizzaStoreAdminApplication,
    private val firebaseService: FirebaseService,
    private val authService: AuthService,
    private val pizzaDao: PizzaDao,
    private val remoteMapper: RemoteMapper,
    private val localMapper: LocalMapper
) : PizzaStoreRepository {

    private val currentProduct = MutableStateFlow(Product())
    private val currentCity = MutableStateFlow(City())
    private val currentOrder = MutableStateFlow(Order())
    private val currentUser = MutableStateFlow<User?>(null)

    private val dbResponseFlow: MutableStateFlow<DBResponse> =
        MutableStateFlow(DBResponse.Processing)

    private val typeFlow: MutableStateFlow<PictureType?> = MutableStateFlow(PictureType.PIZZA)
    private val currentPictureUriFlow: MutableStateFlow<Uri?> = MutableStateFlow(null)

    private val _signInEvents = MutableSharedFlow<SignInEvents>()
    override val signInEvents: SharedFlow<SignInEvents>
        get() = _signInEvents.asSharedFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            subscribePicturesTypeFlow()
        }
        CoroutineScope(Dispatchers.IO).launch {
            subscribeListProducts()
        }
        CoroutineScope(Dispatchers.IO).launch {
            initUserFlow()
        }
        CoroutineScope(Dispatchers.IO).launch {
            subscribeAuthFlow()
        }
        subscribeOrdersUpdates()
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
    override suspend fun getListPicturesUseCase() = firebaseService.getListUriFlow()
    //</editor-fold>

    //<editor-fold desc="getCitiesUseCase">
    override fun getCitiesUseCase(): Flow<List<City>> = firebaseService.getListCitiesFlow()
    //</editor-fold>

    //<editor-fold desc="getProductsUseCase">
    override fun getProductsUseCase(): Flow<List<Product>> =
        firebaseService.getListProductsFlow()
    //</editor-fold>

    //<editor-fold desc="getOrdersUseCase">
    override suspend fun getOrdersUseCase(): Flow<List<Order>> =
        firebaseService.getListOrdersFlow()
            .map { ordersDto ->

                val productsDbModel = pizzaDao.getProductsOneTime()
                val products = mutableListOf<Product>()
                productsDbModel?.forEach { currentProductModel ->
                    val currentProduct = localMapper.dbModelToProduct(currentProductModel)
                    products.add(currentProduct)
                }

                val orders = mutableListOf<Order>()
                ordersDto.forEach { orderDto ->
                    val currentOrder = remoteMapper.mapOrderDtoToEntity(orderDto, products)
                    if (currentOrder != null) {
                        orders.add(currentOrder)
                    }
                }
                addOrdersToLocalDb(orders)
                orders

            }
    //</editor-fold>

    //<editor-fold desc="addOrdersToLocalDb">
    private suspend fun addOrdersToLocalDb(orders: List<Order>) {
        val listProducts = mutableListOf<Product>()
        val productsListModel = pizzaDao.getProductsOneTime()

        productsListModel?.forEach { productDbModel ->
            val currentProduct = localMapper.dbModelToProduct(productDbModel)
            listProducts.add(currentProduct)
        }

        val listModelOrders = mutableListOf<OrderDbModel>()
        orders.forEach { orderFromList ->
            val currentOrder = localMapper.mapOrderToOrderModel(orderFromList)
            listModelOrders.add(currentOrder)
        }
        pizzaDao.addOrders(listModelOrders)
    }
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
            .collect { products ->
                val productsModelList = mutableListOf<ProductDbModel>()
                products.forEach { currentProduct ->
                    val productModel = localMapper.mapProductToDbModel(currentProduct)
                    productsModelList.add(productModel)
                }
                pizzaDao.addProducts(productsModelList)
            }
    }
    //</editor-fold>

    //<editor-fold desc="editOrderUseCase">
    override fun editOrderUseCase(order: Order) {
        val orderDto = remoteMapper.mapOrderToOrderDto(order)
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

    //<editor-fold desc="subscribeOrdersUpdates">
    private fun subscribeOrdersUpdates() {
        WorkManager.getInstance(application).enqueueUniqueWork(
            LoadOrdersWorker.NAME,
            ExistingWorkPolicy.REPLACE,
            LoadOrdersWorker.makeRequest()
        )
    }
    //</editor-fold>

    //<editor-fold desc="initUserFlow">
    private suspend fun initUserFlow() {
        val userFromLocalDb = pizzaDao.getUser()
        if (userFromLocalDb != null) {
            val currentUsersBase = firebaseService.getUsers()
            val currentUserFromBase = currentUsersBase.first {
                it.id == userFromLocalDb.id
            }
            val userDtoToEntity = remoteMapper.mapUserDtoToEntity(currentUserFromBase)
            val userModelToEntity = localMapper.mapDbModelToUser(userFromLocalDb)
            if (userModelToEntity != userDtoToEntity) {
                pizzaDao.putUser(localMapper.mapUserToDbModel(userDtoToEntity))
            }
            currentUser.value = localMapper.mapDbModelToUser(userFromLocalDb)
            return
        }
    }
    //</editor-fold>

    //<editor-fold desc="subscribeAuthFlow">
    private suspend fun subscribeAuthFlow() {
        authService.authFlow.collect {
            when (it) {
                is AuthResponse.Failed -> {
                    when (it.failReason) {
                        AuthResponse.Failed.FailReason.ErrorAuth -> {
                            _signInEvents.emit(
                                SignInEvents.Failed(
                                    failReason = SignInEvents.Failed.FailReason.ErrorSignIn
                                )
                            )
                        }

                        AuthResponse.Failed.FailReason.NoCredentials -> {
                            _signInEvents.emit(
                                SignInEvents.Failed(
                                    failReason = SignInEvents.Failed.FailReason.NoCredentials
                                )
                            )
                        }

                        AuthResponse.Failed.FailReason.UserCancelled -> {
//                            _signInEvents.emit(
//                                SignInEvents.Failed(
//                                    failReason = SignInEvents.Failed.FailReason.UserCancelled
//                                )
//                            )
                        }
                    }
                }

                is AuthResponse.Success -> {
                    _signInEvents.emit(
                        SignInEvents.Success
                    )
                }
            }
        }
    }
    //</editor-fold>

    override fun getUserUseCase() = currentUser.asStateFlow()

    //<editor-fold desc="logOut">
    override fun signOut() {
        currentUser.value = null
    }
    //</editor-fold>

    override suspend fun signInWithEmail(email: String, password: String) =
        authService.signInWithEmail(email, password)

    override suspend fun signInWithSavedAccounts() =
        authService.signInWithSavedAccounts()
}