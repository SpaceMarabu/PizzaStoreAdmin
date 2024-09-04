package com.example.pizzastoreadmint.domain.usecases.business

import com.example.pizzastoreadmint.domain.entity.Order
import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import javax.inject.Inject

class EditOrderUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun editOrder(order: Order) {
        repository.editOrderUseCase(order)
    }
}