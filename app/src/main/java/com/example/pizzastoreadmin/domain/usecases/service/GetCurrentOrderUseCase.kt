package com.example.pizzastoreadmin.domain.usecases.service

import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentOrderUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getOrder(): StateFlow<Order?> {
        return repository.getCurrentOrderUseCase()
    }
}