package com.example.pizzastoreadmint.domain.usecases.business

import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCitiesUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getCitiesFlow(): Flow<List<City>> {
        return repository.getCitiesUseCase()
    }
}