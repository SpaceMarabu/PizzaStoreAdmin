package com.example.pizzastoreadmin.data.firebasedb

import android.util.Log
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.entity.ProductType
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppDatabase : FirebaseService {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val dRefCities = firebaseDatabase.getReference("cities")
    private val dRefProduct = firebaseDatabase.getReference("product")

    private val firebaseStorage = Firebase.storage("gs://pizzastore-b379f.appspot.com")
    private val storageRef = firebaseStorage.reference.child("product")

    private val maxProductIdFlow = MutableStateFlow(-1)
    private val maxCityIdFlow = MutableStateFlow(-1)

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
    override fun getListProductsFlow(): Flow<List<Product>> = listProductsFlow

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
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    deferred.complete(DBResponse.Complete)
                })
                .addOnFailureListener(OnFailureListener { e ->
                    deferred.complete(
                        DBResponse.Error("Не удалось изменить данные. $e")
                    )
                })
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
                    .addOnFailureListener(OnFailureListener { _ ->
                        haveErrors = true
                    })
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

    override fun getListCitiesFlow(): Flow<List<City>> = listCitiesFlow

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
                    .addOnFailureListener(OnFailureListener { _ ->
                        haveErrors = true
                    })
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
}