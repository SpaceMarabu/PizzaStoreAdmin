package com.example.pizzastoreadmint.domain.usecases.service

import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
import javax.inject.Inject

class SetCurrentCityUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun setCity(city: City?) {
        repository.setCurrentCityUseCase(city)
    }
}