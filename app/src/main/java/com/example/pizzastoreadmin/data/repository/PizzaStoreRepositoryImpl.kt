package com.example.pizzastoreadmin.data.repository

import android.util.Log
import com.example.pizzastore.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject


class PizzaStoreRepositoryImpl @Inject constructor(
) : PizzaStoreRepository {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val dRef = firebaseDatabase.getReference("cities")

    private val listCitiesFlow = callbackFlow {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listCities = mutableListOf<City>()
                for (data in dataSnapshot.children) {
                    val key: String = data.key ?: continue
                    val value = data.getValue(City::class.java) ?: continue
                    listCities.add(
                        City(
                            id = key.toInt(),
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

    override fun getCitiesUseCase(): Flow<List<City>> {
        return listCitiesFlow
    }

    override fun addOrEditCityUseCase(city: City) {
        val cityId = city.id.toString()
        dRef.child(cityId)
            .push()
            .setValue(city)
            .addOnSuccessListener(OnSuccessListener<Void?> {
                dRef.child(cityId)
                    .push()
                    .setValue(city)
                    .addOnSuccessListener(OnSuccessListener<Void?> { })
                    .addOnFailureListener(OnFailureListener { e ->
                        Log.d(
                            "ERROR",
                            "добавить поток, куда полетят ошибки"
                        )
                    })
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.d(
                    "ERROR",
                    "добавить поток, куда полетят ошибки"
                )
            })
    }

    override fun deleteCitiesUseCase(cities: List<City>) {
        cities.forEach {city ->
            val cityId = city.id.toString()
            Log.d("TEST_TEST", "$cityId Delete")
//            dRef.child(cityId)
//                .removeValue()
        }
    }
}