package com.example.pizzastoreadmin.data.repository

import android.net.Uri
import android.util.Log
import com.example.pizzastoreadmin.data.firebasedb.FirebaseService
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class PizzaStoreRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : PizzaStoreRepository {

    private val firebaseStorage = Firebase.storage("gs://pizzastore-b379f.appspot.com")
    private val storageRef = firebaseStorage.reference.child("product")

    private val currentProduct: MutableStateFlow<Product> = MutableStateFlow(Product())
    private val currentCity: MutableStateFlow<City> = MutableStateFlow(City())

    private val dbResponseFlow: MutableStateFlow<DBResponse> =
        MutableStateFlow(DBResponse.Processing)

    private val typeFlow: MutableStateFlow<PictureType?> = MutableStateFlow(null)
    private val listPicturesUriFlow: MutableSharedFlow<List<Uri>> = MutableSharedFlow(replay = 1)
    private val currentPictureUriFlow: MutableStateFlow<Uri?> = MutableStateFlow(null)


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

    override fun getCitiesUseCase(): Flow<List<City>> = firebaseService.getListCitiesFlow()

    override fun getProductsUseCase(): Flow<List<Product>> = firebaseService.getListProductsFlow()

    override fun setCurrentCityUseCase(city: City?) {
        currentCity.value = city ?: City()
    }

    override fun getCurrentCityUseCase() = currentCity.asStateFlow()

    //<editor-fold desc="addOrEditCityUseCase">
    override fun addOrEditCityUseCase(city: City) {

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
            dbResponseFlow.emit(
                firebaseService.addOrEditCity(city)
            )
        }
    }
    //</editor-fold>

    //<editor-fold desc="deleteCitiesUseCase">
    override fun deleteCitiesUseCase(cities: List<City>) {

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
            dbResponseFlow.emit(
                firebaseService.deleteCity(cities)
            )
        }
    }
//</editor-fold>

    //<editor-fold desc="addOrEditProductUseCase">
    override fun addOrEditProductUseCase(product: Product) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
            dbResponseFlow.emit(
                firebaseService.addOrEditProduct(product)
            )
        }
    }
    //</editor-fold>

    //<editor-fold desc="deleteProductsUseCase">
    override fun deleteProductsUseCase(products: List<Product>) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
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
}