package com.example.pizzastoreadmint.domain.usecases.business

import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
import com.example.pizzastoreadmint.domain.entity.Product
import javax.inject.Inject

class AddOrEditProductUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun addOrEditProduct(product: Product) {
        repository.addOrEditProductUseCase(product)
    }
}