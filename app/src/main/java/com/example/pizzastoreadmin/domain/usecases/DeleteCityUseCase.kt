package com.example.pizzastoreadmin.domain.usecases

import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import javax.inject.Inject

class DeleteCityUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun deleteCity(cities: List<City>) {
        repository.deleteCitiesUseCase(cities)
    }
}