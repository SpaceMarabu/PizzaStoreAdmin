package com.example.pizzastoreadmin.domain.usecases.service

import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.Product
import javax.inject.Inject

class SetCurrentProductUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun setProduct(product: Product?) {
        repository.setCurrentProductUseCase(product)
    }
}