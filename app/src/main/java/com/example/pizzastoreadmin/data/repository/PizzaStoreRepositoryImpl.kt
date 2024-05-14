package com.example.pizzastoreadmin.data.repository

import android.net.Uri
import android.util.Log
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.entity.ProductType
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class PizzaStoreRepositoryImpl @Inject constructor(
) : PizzaStoreRepository {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val dRefCities = firebaseDatabase.getReference("cities")
    private val dRefProduct = firebaseDatabase.getReference("product")

    private val firebaseStorage = Firebase.storage("gs://pizzastore-b379f.appspot.com")
    private val storageRef = firebaseStorage.reference.child("product")

    private val currentProduct: MutableStateFlow<Product> = MutableStateFlow(Product())

    private val currentCity: MutableStateFlow<City> = MutableStateFlow(City())
    private val maxCityIdFlow = MutableStateFlow(-1)
    private val maxProductIdFlow = MutableStateFlow(-1)
    private val dbResponseFlow: MutableStateFlow<DBResponse> =
        MutableStateFlow(DBResponse.Processing)

    private val typeFlow: MutableStateFlow<PictureType?> = MutableStateFlow(null)
    private val listPicturesUriFlow: MutableSharedFlow<List<Uri>> = MutableSharedFlow(replay = 1)
    private val currentPictureUriFlow: MutableStateFlow<Uri?> = MutableStateFlow(null)


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

    //<editor-fold desc="listProductsFlow">
    private val listProductsFlow = callbackFlow {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listProducts = mutableListOf<Product>()
                maxProductIdFlow.value = -1
                for (dataFromChildren in dataSnapshot.children) {
                    dataFromChildren.key
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

    init {
        CoroutineScope(Dispatchers.IO).launch {
            subscribePicturesTypeFlow()
        }
    }

    //<editor-fold desc="getListPictures">
    private suspend fun getListPictures(type: String) =
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
    //</editor-fold>

    override suspend fun postPicturesType(type: PictureType) = typeFlow.emit(type)

    //<editor-fold desc="deletePicturesUseCase">
    override fun deleteImagesUseCase(listToDelete: List<Uri>) {
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
                    .addOnSuccessListener {
                        val currentType = typeFlow.value?.type
                        CoroutineScope(Dispatchers.IO).launch {
                            if (currentType != null) {
                                getListPictures(currentType)
                            }
                        }
                    }
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
                getListPictures(it.type)
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="putImageToStorage">
    override fun putImageToStorage(name: String, type: String, imageByte: ByteArray) {
        var counter = 1
        var nameToPut = name
        var needNextCheck = true
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            while (needNextCheck) {
                val isAvailableName = checkAvailabilityName(nameToPut, type)
                Log.d("TEST_TEST", isAvailableName.toString())
                if (isAvailableName) {
                    needNextCheck = false
                } else {
                    nameToPut = "${name}${counter++}"
                }
            }
            dbResponseFlow.value = DBResponse.Processing
            storageRef
                .child(type)
                .child(nameToPut)
                .putBytes(imageByte)
                .addOnSuccessListener {
                    dbResponseFlow.value = DBResponse.Complete
                }
                .addOnFailureListener {
                    dbResponseFlow.value = DBResponse.Error(it.toString())
                }
        }
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

    override suspend fun getListImagesUseCase() = listPicturesUriFlow.asSharedFlow()

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

    override fun getCitiesUseCase(): Flow<List<City>> = listCitiesFlow

    override fun getProductsUseCase(): Flow<List<Product>> = listProductsFlow

    override fun setCurrentCityUseCase(city: City?) {
        currentCity.value = city ?: City()
    }

    override fun getCurrentCityUseCase() = currentCity.asStateFlow()

    //<editor-fold desc="addOrEditCityUseCase">
    override fun addOrEditCityUseCase(city: City) {

        var currentCity = city
        val currentIdToInsert = (maxCityIdFlow.value + 1).toString()
        val cityId = if (city.id == -1) {
            currentCity = currentCity.copy(id = currentIdToInsert.toInt())
            currentIdToInsert
        } else {
            city.id.toString()
        }

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
            dRefCities.child(cityId)
                .setValue(currentCity)
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    dbResponseFlow.value = DBResponse.Complete
                    scope.cancel()
                })
                .addOnFailureListener(OnFailureListener { e ->
                    dbResponseFlow.value =
                        DBResponse.Error("Не удалось изменить данные. $e")
                    scope.cancel()
                })
        }
    }
//</editor-fold>

    //<editor-fold desc="deleteCitiesUseCase">
    override fun deleteCitiesUseCase(cities: List<City>) {
        var haveErrors = false
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
            cities.forEach { city ->
                val cityId = city.id.toString()
                dRefCities.child(cityId)
                    .removeValue()
                    .addOnFailureListener(OnFailureListener { _ ->
                        haveErrors = true
                    })
            }
            dbResponseFlow.emit(
                if (haveErrors) {
                    DBResponse.Error("Ошибка удаления")
                } else {
                    DBResponse.Complete
                }
            )
        }
    }
//</editor-fold>

    //<editor-fold desc="addOrEditProductUseCase">
    override fun addOrEditProductUseCase(product: Product) {

        var currentProduct = product
        val currentIdToInsert = maxProductIdFlow.value + 1
        val productId = if (product.id == -1) {
            currentProduct = currentProduct.copy(id = currentIdToInsert)
            currentIdToInsert.toString()
        } else {
            product.id.toString()
        }

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
            dRefProduct.child(productId)
                .setValue(currentProduct)
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    dbResponseFlow.value = DBResponse.Complete
                    scope.cancel()
                })
                .addOnFailureListener(OnFailureListener { e ->
                    dbResponseFlow.value =
                        DBResponse.Error("Не удалось изменить данные. $e")
                    scope.cancel()
                })
        }
    }
    //</editor-fold>

    //<editor-fold desc="deleteProductsUseCase">
    override fun deleteProductsUseCase(products: List<Product>) {
        var haveErrors = false
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
            products.forEach { product ->
                val productId = product.id.toString()
                dRefProduct.child(productId)
                    .removeValue()
                    .addOnFailureListener(OnFailureListener { _ ->
                        haveErrors = true
                    })
            }
            dbResponseFlow.emit(
                if (haveErrors) {
                    DBResponse.Error("Ошибка удаления")
                } else {
                    DBResponse.Complete
                }
            )
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
}