package com.example.pizzastoreadmin.presentation.order.orders

import com.example.pizzastoreadmin.domain.entity.OrderStatus

data class FilterState(
    val filterMap: Map<OrderStatus, Boolean>
) {
    companion object {

        fun initFilter() = FilterState(
            mapOf(
                OrderStatus.NEW to false,
                OrderStatus.PROCESSING to false,
                OrderStatus.FINISH to false,
                OrderStatus.ACCEPT to false
            )
        )
    }
}
