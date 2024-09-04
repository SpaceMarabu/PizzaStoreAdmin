package com.example.pizzastoreadmint.domain.usecases.service

import com.example.pizzastoreadmint.domain.entity.Product
import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentProductUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getProduct(): StateFlow<Product> {
        return repository.getCurrentProductUseCase()
    }
}