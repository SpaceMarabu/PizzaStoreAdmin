package com.example.pizzastoreadmin.data.repository

import android.net.Uri
import android.util.Log
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class PizzaStoreRepositoryImpl @Inject constructor(
) : PizzaStoreRepository {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val dRefCities = firebaseDatabase.getReference("cities")
    private val dRefPictrues = firebaseDatabase.getReference("pictures")

    private val firebaseStorage = Firebase.storage("gs://pizzastore-b379f.appspot.com")
    private val storageRef = firebaseStorage.reference.child("product")

    private val currentCity: MutableStateFlow<City> = MutableStateFlow(City())
    private val maxCityIdFlow = MutableStateFlow(-1)
    private val dbResponseFlow: MutableStateFlow<DBResponse> =
        MutableStateFlow(DBResponse.Processing)

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
                    addInfoAboutPicture(type, nameToPut)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getListPicturesUseCase(): HashMap<String, List<Uri>> {
        val resultMap = HashMap<String, List<Uri>>()
        withContext(Dispatchers.IO) {
            val types = listOf(
                PictureType.PIZZA,
                PictureType.ROLL,
                PictureType.STARTER,
                PictureType.DESSERT,
                PictureType.DRINK,
                PictureType.STORY
            )
            types.forEach typesForEach@{ pictureType ->
                val deferredTemp = CompletableDeferred<List<Uri>>()
                storageRef
                    .child(pictureType.type)
                    .listAll()
                    .addOnSuccessListener { result ->
                        val tempUriListByCurrentType = mutableListOf<Uri>()
                        result.items.forEach { storRef ->
                            val scope = CoroutineScope(Dispatchers.IO)
                            val uriDeffered = CompletableDeferred<Uri>()
                            scope.launch {
                                storRef
                                    .downloadUrl
                                    .addOnSuccessListener {
                                        if (it != null) {
                                            uriDeffered.complete(it)
                                        }
                                    }
                                uriDeffered.invokeOnCompletion {
                                    tempUriListByCurrentType.add(uriDeffered.getCompleted())
                                }
                                uriDeffered.await()
                            }
                        }
                        deferredTemp.complete(tempUriListByCurrentType)
                    }
                deferredTemp.await()
                deferredTemp.invokeOnCompletion {
                    resultMap[pictureType.type] = deferredTemp.getCompleted()
                }
            }
        }
        return resultMap
    }

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

    override fun getCitiesUseCase(): Flow<List<City>> {
        return listCitiesFlow
    }

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

    //<editor-fold desc="addInfoAboutPicture">
    fun addInfoAboutPicture(type: String, name: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.emit(DBResponse.Processing)
            dRefPictrues
                .child(type)
                .push()
                .setValue(name)
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    scope.cancel()
                })
                .addOnFailureListener(OnFailureListener { e ->
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

    override fun getDbResponse(): StateFlow<DBResponse> {
        dbResponseFlow.value = DBResponse.Processing
        return dbResponseFlow.asStateFlow()
    }
}