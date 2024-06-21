package com.example.pizzastoreadmin.domain.usecases.business

import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import javax.inject.Inject

class EditOrderUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun editOrder(order: Order) {
        repository.editOrderUseCase(order)
    }
}