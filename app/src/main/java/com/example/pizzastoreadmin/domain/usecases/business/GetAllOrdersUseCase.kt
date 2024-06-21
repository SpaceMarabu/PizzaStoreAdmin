package com.example.pizzastoreadmin.domain.usecases.business

import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllOrdersUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getOrdersFlow(): Flow<List<Order>> {
        return repository.getOrdersUseCase()
    }
}