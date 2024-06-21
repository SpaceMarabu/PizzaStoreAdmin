package com.example.pizzastoreadmin.presentation.order.orders

import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.OrderStatus

sealed class OrderScreenState() {

    object Initial : OrderScreenState()
    object Loading : OrderScreenState()

    data class Content(
        val orders: List<Order>,
        val statusFilter: List<OrderStatus>
    ): OrderScreenState()

}
