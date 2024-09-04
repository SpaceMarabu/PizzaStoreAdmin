package com.example.pizzastoreadmint.domain.usecases.service

import com.example.pizzastoreadmint.domain.entity.Order
import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import javax.inject.Inject

class SetCurrentOrderUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun setOrder(order: Order) {
        repository.setCurrentOrderUseCase(order)
    }
}