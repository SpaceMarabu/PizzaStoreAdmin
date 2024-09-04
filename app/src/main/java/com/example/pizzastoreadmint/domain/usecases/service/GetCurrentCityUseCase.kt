package com.example.pizzastoreadmint.domain.usecases.service

import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentCityUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getCity(): StateFlow<City> {
        return repository.getCurrentCityUseCase()
    }
}