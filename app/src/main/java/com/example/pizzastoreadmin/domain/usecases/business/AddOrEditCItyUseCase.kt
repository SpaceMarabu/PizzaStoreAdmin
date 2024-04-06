package com.example.pizzastoreadmin.domain.usecases.business

import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import javax.inject.Inject

class AddOrEditCItyUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun addOrEditCity(city: City) {
        repository.addOrEditCityUseCase(city)
    }
}