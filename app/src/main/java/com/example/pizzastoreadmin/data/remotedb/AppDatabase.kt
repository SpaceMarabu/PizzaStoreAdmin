package com.example.pizzastoreadmin.data.remotedb

import android.net.Uri
import android.util.Log
import com.example.pizzastoreadmin.data.remotedb.entity.BucketDto
import com.example.pizzastoreadmin.data.remotedb.entity.OrderDto
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.entity.ProductType
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppDatabase : FirebaseService {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val dRefCities = firebaseDatabase.getReference(CITIES_REFERENCE)
    private val dRefProduct = firebaseDatabase.getReference(PRODUCT_REFERENCE)
    private val dRefOrder = firebaseDatabase.getReference(ORDER_REFERENCE)

    private val firebaseStorage = Firebase.storage(STORAGE_REFERENCE)
    private val storageRef = firebaseStorage.reference.child(PRODUCT_REFERENCE)

    private val maxProductIdFlow = MutableStateFlow(-1)
    private val maxCityIdFlow = MutableStateFlow(-1)

    private val listPicturesUriFlow: MutableSharedFlow<List<Uri>> = MutableSharedFlow(replay = 1)

    //<editor-fold desc="listProductsFlow">
    private val listProductsFlow = callbackFlow {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listProducts = mutableListOf<Product>()
                maxProductIdFlow.value = -1
                for (dataFromChildren in dataSnapshot.children) {
                    val key: Int = dataFromChildren.key?.toInt() ?: continue
                    if (maxProductIdFlow.value < key) {
                        maxProductIdFlow.value = key
                    }

                    val id = dataFromChildren.child("id").value.toString().toInt()
                    val name = dataFromChildren.child("name").value.toString()
                    val typeFromSnapshot = dataFromChildren.child("type").child("type").value
                    val price = dataFromChildren.child("price").value.toString().toInt()
                    val photo = dataFromChildren.child("photo").value.toString()
                    val description = dataFromChildren.child("description").value.toString()
                    val typeObject = when (typeFromSnapshot) {
                        ProductType.PIZZA.type -> ProductType.PIZZA
                        ProductType.DESSERT.type -> ProductType.DESSERT
                        ProductType.STARTER.type -> ProductType.STARTER
                        ProductType.DRINK.type -> ProductType.DRINK
                        else -> ProductType.ROLL
                    }
                    listProducts.add(
                        Product(
                            id = id,
                            name = name,
                            type = typeObject,
                            price = price,
                            photo = photo,
                            description = description
                        )
                    )
                }
                val returnList = listProducts.toList()
                trySend(returnList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(
                    "PizzaStoreRepositoryImpl",
                    "loadCities:onCancelled",
                    databaseError.toException()
                )
            }
        }
        dRefProduct.addValueEventListener(postListener)

        awaitClose {
            dRefProduct.removeEventListener(postListener)
        }
    }
    //</editor-fold>

    //<editor-fold desc="listCitiesFlow">
    private val listCitiesFlow = callbackFlow {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listCities = mutableListOf<City>()
                maxCityIdFlow.value = -1
                for (dataFromChildren in dataSnapshot.children) {
                    dataFromChildren.key
                    val key: Int = dataFromChildren.key?.toInt() ?: continue
                    if (maxCityIdFlow.value < key) {
                        maxCityIdFlow.value = key
                    }
                    val value = dataFromChildren.getValue(City::class.java) ?: continue
                    listCities.add(
                        City(
                            id = key,
                            name = value.name,
                            //filterNotNull - не бесполезный
                            points = value.points.filterNotNull()
                        )
                    )
                }
                val returnList = listCities.toList()
                trySend(returnList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(
                    "PizzaStoreRepositoryImpl",
                    "loadCities:onCancelled",
                    databaseError.toException()
                )
            }
        }
        dRefCities.addValueEventListener(postListener)

        awaitClose {
            dRefCities.removeEventListener(postListener)
        }
    }

    //</editor-fold>

    //<editor-fold desc="getListOrdersFlow">
    override fun getListOrdersFlow() = callbackFlow {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listOrders = mutableListOf<OrderDto>()
                for (dataFromChildren in dataSnapshot.children) {
                    val key: Int = dataFromChildren.key?.toInt() ?: continue

                    val status = dataFromChildren.child("status").value.toString()
                    val bucket = dataFromChildren
                        .child("bucket").getValue(BucketDto::class.java) ?: BucketDto()
                    listOrders.add(
                        OrderDto(
                            id = key,
                            status = status,
                            bucket = bucket
                        )
                    )
                }
                val returnList = listOrders.toList()
                trySend(returnList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(
                    "PizzaStoreFirebaseImpl",
                    "loadCities:onCancelled",
                    databaseError.toException()
                )
            }
        }
        dRefOrder.addValueEventListener(postListener)

        awaitClose {
            dRefOrder.removeEventListener(postListener)
        }
    }
    //</editor-fold>

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getListOrdersOneTime(): List<OrderDto> {

        val deferred = CompletableDeferred<List<OrderDto>>()
        withContext(Dispatchers.IO) {
            dRefOrder.get().addOnSuccessListener { dataSnapshot ->
                val listOrders = mutableListOf<OrderDto>()
                for (dataFromChildren in dataSnapshot.children) {
                    val key: Int = dataFromChildren.key?.toInt() ?: continue

                    val status = dataFromChildren.child("status").value.toString()
                    val bucket = dataFromChildren
                        .child("bucket").getValue(BucketDto::class.java) ?: BucketDto()
                    listOrders.add(
                        OrderDto(
                            id = key,
                            status = status,
                            bucket = bucket
                        )
                    )
                }
                deferred.complete(listOrders.toList())
            }
            deferred.await()
        }
        return deferred.getCompleted()
    }

    //<editor-fold desc="getListProductsFlow">
    override fun getListProductsFlow(): Flow<List<Product>> = listProductsFlow
    //</editor-fold>

    //<editor-fold desc="addOrEditProduct">
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun addOrEditProduct(product: Product): DBResponse {

        var currentProduct = product
        val currentIdToInsert = maxProductIdFlow.value + 1
        val productId = if (product.id == -1) {
            currentProduct = currentProduct.copy(id = currentIdToInsert)
            currentIdToInsert.toString()
        } else {
            product.id.toString()
        }

        val deferred = CompletableDeferred<DBResponse>()
        withContext(Dispatchers.IO) {
            dRefProduct.child(productId)
                .setValue(currentProduct)
                .addOnSuccessListener {
                    deferred.complete(DBResponse.Complete)
                }
                .addOnFailureListener { e ->
                    deferred.complete(
                        DBResponse.Error("Не удалось изменить данные. $e")
                    )
                }
            deferred.await()
        }
        return deferred.getCompleted()
    }
    //</editor-fold>

    //<editor-fold desc="deleteProduct">
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun deleteProduct(products: List<Product>): DBResponse {
        val deferred = CompletableDeferred<DBResponse>()
        withContext(Dispatchers.IO) {
            var haveErrors = false
            products.forEach { product ->
                val productId = product.id.toString()
                dRefProduct.child(productId)
                    .removeValue()
                    .addOnFailureListener { _ ->
                        haveErrors = true
                    }
            }
            deferred.await()
            deferred.complete(
                if (haveErrors) {
                    DBResponse.Error("Ошибка удаления")
                } else {
                    DBResponse.Complete
                }
            )
        }
        return deferred.getCompleted()
    }
    //</editor-fold>

    //<editor-fold desc="getListCitiesFlow">
    override fun getListCitiesFlow(): Flow<List<City>> = listCitiesFlow
    //</editor-fold>

    //<editor-fold desc="addOrEditCity">
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun addOrEditCity(city: City): DBResponse {
        var currentCity = city
        val currentIdToInsert = (maxCityIdFlow.value + 1).toString()
        val cityId = if (city.id == -1) {
            currentCity = currentCity.copy(id = currentIdToInsert.toInt())
            currentIdToInsert
        } else {
            city.id.toString()
        }

        val deferred = CompletableDeferred<DBResponse>()
        withContext(Dispatchers.IO) {
            dRefCities.child(cityId)
                .setValue(currentCity)
                .addOnSuccessListener {
                    deferred.complete(DBResponse.Complete)
                }
                .addOnFailureListener { e ->
                    deferred.complete(
                        DBResponse.Error("Не удалось изменить данные. $e")
                    )
                }
            deferred.await()
        }
        return deferred.getCompleted()
    }
    //</editor-fold>

    //<editor-fold desc="deleteCity">
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun deleteCity(cities: List<City>): DBResponse {
        var haveErrors = false
        val deferred = CompletableDeferred<DBResponse>()
        withContext(Dispatchers.IO) {
            cities.forEach { city ->
                val cityId = city.id.toString()
                dRefCities.child(cityId)
                    .removeValue()
                    .addOnFailureListener { _ ->
                        haveErrors = true
                    }
            }
            deferred.complete(
                if (haveErrors) {
                    DBResponse.Error("Ошибка удаления")
                } else {
                    DBResponse.Complete
                }
            )
            deferred.await()
        }
        return deferred.getCompleted()
    }
    //</editor-fold>

    //<editor-fold desc="loadPictures">
    override suspend fun loadPictures(type: String) {
        withContext(Dispatchers.IO) {
            val scope = CoroutineScope(Dispatchers.IO)
            storageRef
                .child(type)
                .listAll()
                .addOnSuccessListener { result ->
                    val tempUriList = mutableListOf<Uri>()
                    scope.launch {
                        result
                            .items
                            .forEach { storageReference ->
                                val uri = getUriByStorageReference(storageReference)
                                tempUriList.add(uri)
                            }
                        listPicturesUriFlow.emit(tempUriList)
                    }
                }
        }
    }
    //</editor-fold>

    //<editor-fold desc="getUriByStorageReference">
    private suspend fun getUriByStorageReference(storageReference: StorageReference) =
        withContext(Dispatchers.IO) {
            val deferred = CompletableDeferred<Uri>()
            storageReference
                .downloadUrl
                .addOnSuccessListener {
                    if (it != null) {
                        deferred.complete(it)
                    }
                }
            deferred.await()
        }
//</editor-fold>

    //<editor-fold desc="checkAvailabilityName">
    private suspend fun checkAvailabilityName(name: String, type: String) =
        withContext(Dispatchers.IO) {
            var isAvailable = true
            val regexTemplate = Regex("\\..+")
            val deferred = CompletableDeferred<Boolean>()
            storageRef
                .child(type)
                .listAll()
                .addOnSuccessListener { result ->
                    result.items.forEach { item ->
                        val nameWithoutExtension = item.name.replace(regexTemplate, "")
                        if (nameWithoutExtension == name) {
                            isAvailable = false
                            return@forEach
                        }
                    }
                    deferred.complete(isAvailable)
                }
            deferred.await()
        }
//</editor-fold>

    //<editor-fold desc="putImageToStorage">
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun putImageToStorage(
        name: String,
        type: String,
        imageByte: ByteArray
    ): DBResponse {
        val deferred = CompletableDeferred<DBResponse>()
        withContext(Dispatchers.IO) {
            var counter = 1
            var nameToPut = name
            var needNextCheck = true
            while (needNextCheck) {
                val isAvailableName = checkAvailabilityName(nameToPut, type)
                if (isAvailableName) {
                    needNextCheck = false
                } else {
                    nameToPut = "${name}${counter++}"
                }
            }
            storageRef
                .child(type)
                .child(nameToPut)
                .putBytes(imageByte)
                .addOnSuccessListener {
                    deferred.complete(DBResponse.Complete)
                }
                .addOnFailureListener {
                    deferred.complete(DBResponse.Error(it.toString()))
                }
            deferred.await()
        }
        return deferred.getCompleted()
    }
    //</editor-fold>

    //<editor-fold desc="getListUriFlow">
    override fun getListUriFlow() = listPicturesUriFlow.asSharedFlow()
    //</editor-fold>

    //<editor-fold desc="deletePictures">
    override suspend fun deletePictures(listToDelete: List<Uri>): Boolean {
        listToDelete.forEach {
            val uriString = it.toString()
            val startSubstring = uriString.indexOf("/product")
            val endSubstring = uriString.indexOf("?alt")
            val currentAddressToDelete = uriString
                .substring(startSubstring, endSubstring)
                .replace("%2F", "/")
                .replace("/product", "")
            CoroutineScope(Dispatchers.IO).launch {
                storageRef.child(currentAddressToDelete)
                    .delete()
            }
        }
        return true
    }
    //</editor-fold>

    //<editor-fold desc="editOrder">
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun editOrder(orderDto: OrderDto): DBResponse {

        val deferred = CompletableDeferred<DBResponse>()
        withContext(Dispatchers.IO) {
            dRefOrder.child(orderDto.id.toString())
                .setValue(orderDto)
                .addOnSuccessListener {
                    deferred.complete(DBResponse.Complete)
                }
                .addOnFailureListener { e ->
                    deferred.complete(
                        DBResponse.Error("Что-то пошло не так. $e")
                    )
                }
            deferred.await()
        }
        return deferred.getCompleted()
    }
    //</editor-fold>

    companion object {
        private const val CITIES_REFERENCE = "cities"
        private const val PRODUCT_REFERENCE = "product"
        private const val ORDER_REFERENCE = "order"
        private const val STORAGE_REFERENCE = "gs://pizzastore-b379f.appspot.com"
    }

}