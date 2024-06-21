package com.example.pizzastoreadmin.domain.usecases.service

import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import javax.inject.Inject

class SetCurrentOrderUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun setOrder(order: Order) {
        repository.setCurrentOrderUseCase(order)
    }
}