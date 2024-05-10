package com.example.pizzastoreadmin.domain.usecases.business

import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllProductsUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getProductsFlow(): Flow<List<Product>> {
        return repository.getProductsUseCase()
    }
}