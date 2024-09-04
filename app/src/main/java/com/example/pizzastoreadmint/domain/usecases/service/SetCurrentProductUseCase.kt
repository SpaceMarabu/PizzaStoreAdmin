package com.example.pizzastoreadmint.domain.usecases.service

import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
import com.example.pizzastoreadmint.domain.entity.Product
import javax.inject.Inject

class SetCurrentProductUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun setProduct(product: Product?) {
        repository.setCurrentProductUseCase(product)
    }
}