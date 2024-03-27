package com.example.pizzastoreadmin.domain.usecases

import com.example.pizzastore.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCitiesUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getCitiesFlow(): Flow<List<City>> {
        return repository.getCitiesUseCase()
    }
}