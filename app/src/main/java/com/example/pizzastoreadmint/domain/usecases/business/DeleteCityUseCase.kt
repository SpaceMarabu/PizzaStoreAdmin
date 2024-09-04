package com.example.pizzastoreadmint.domain.usecases.business

import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
import javax.inject.Inject

class DeleteCityUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun deleteCity(cities: List<City>) {
        repository.deleteCitiesUseCase(cities)
    }
}