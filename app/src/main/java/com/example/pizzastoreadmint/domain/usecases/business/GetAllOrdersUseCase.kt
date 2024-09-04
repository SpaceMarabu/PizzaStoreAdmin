package com.example.pizzastoreadmint.domain.usecases.business

import com.example.pizzastoreadmint.domain.entity.Order
import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllOrdersUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    suspend fun getOrdersFlow(): Flow<List<Order>> {
        return repository.getOrdersUseCase()
    }
}