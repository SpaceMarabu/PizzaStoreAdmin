package com.example.pizzastoreadmin.domain.usecases

import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentCityUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getCity(): StateFlow<City> {
        return repository.getCurrentCityUseCase()
    }
}