package com.example.pizzastoreadmin.domain.usecases.service

import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import javax.inject.Inject

class SetCurrentCityUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun setCity(city: City?) {
        repository.setCurrentCityUseCase(city)
    }
}