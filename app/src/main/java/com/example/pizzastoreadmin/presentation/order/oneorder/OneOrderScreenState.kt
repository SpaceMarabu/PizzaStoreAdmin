package com.example.pizzastoreadmin.presentation.order.oneorder

import com.example.pizzastoreadmin.domain.entity.Order

sealed class OneOrderScreenState() {

    object Initial : OneOrderScreenState()
    object Loading : OneOrderScreenState()

    data class Content(
        val order: Order
    ): OneOrderScreenState()

}
