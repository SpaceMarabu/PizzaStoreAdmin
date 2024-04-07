package com.example.pizzastoreadmin.data.repository

import android.util.Log
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class PizzaStoreRepositoryImpl @Inject constructor(
) : PizzaStoreRepository {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val dRef = firebaseDatabase.getReference("cities")

    private val firebaseStorage = Firebase.storage("gs://pizzastore-b379f.appspot.com")
    private val storageRef = firebaseStorage.reference.child("product")

    private val currentCity: MutableStateFlow<City> = MutableStateFlow(City())
    private val maxCityIdFlow = MutableStateFlow(-1)
    private val dbResponseFlow: MutableStateFlow<DBResponse> =
        MutableStateFlow(DBResponse.Processing)

    override fun putImageToStorage(name: String, imageByte: ByteArray) {
        var counter = 1
        var nameToPut = name
        var needNextCheck = true
        while (needNextCheck) {
            if (checkAvailabilityName(nameToPut)) {
                needNextCheck = false
            } else {
                nameToPut = "${nameToPut}${counter++}"
            }
        }
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dbResponseFlow.value = DBResponse.Processing
            storageRef.child(nameToPut)
                .putBytes(imageByte)
                .addOnSuccessListener {
                    dbResponseFlow.value = DBResponse.Complete
                }
                .addOnFailureListener {
                    dbResponseFlow.value = DBResponse.Error(it.toString())
                }
        }
    }

    //<editor-fold desc="checkAvailabilityName">
    private fun checkAvailabilityName(name: String): Boolean {
        var isAvailable = true
        val regexTemplate = Regex("\\..+")
        storageRef.listAll()
            .addOnSuccessListener { items ->
                items.items.forEach { storRef ->
                    val nameWithoutExtension = storRef.name.replace(regexTemplate, "")
                    if (nameWithoutExtension == name) {
                        isAvailable = false
                        return@forEach
                    }
                }
            }
            .addOnFailureListener {
                // Uh-oh, an error occurred!
            }
        return isAvailable
    }
    //</editor-fold>

    //<editor-fold desc="listCitiesFlow">
    private val listCitiesFlow = callbackFlow {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listCities = mutableListOf<City>()
                maxCityIdFlow.value = -1
                for (data in dataSnapshot.children) {
                    val key: Int = data.key?.toInt() ?: continue
                    if (maxCityIdFlow.value < key) {
                        maxCityIdFlow.value = key
                    }
                    val value = data.getValue(City::class.java) ?: continue
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
        dRef.addValueEventListener(postListener)

        awaitClose {
            dRef.removeEventListener(postListener)
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
            dRef.child(cityId)
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
                dRef.child(cityId)
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