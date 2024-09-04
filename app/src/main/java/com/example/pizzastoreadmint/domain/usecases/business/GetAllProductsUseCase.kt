package com.example.pizzastoreadmint.domain.usecases.business

import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
import com.example.pizzastoreadmint.domain.entity.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllProductsUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getProductsFlow(): Flow<List<Product>> {
        return repository.getProductsUseCase()
    }
}