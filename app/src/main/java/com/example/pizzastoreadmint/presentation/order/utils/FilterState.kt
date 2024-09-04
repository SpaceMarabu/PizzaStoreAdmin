package com.example.pizzastoreadmint.presentation.order.utils

import com.example.pizzastoreadmint.domain.entity.OrderStatus

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
