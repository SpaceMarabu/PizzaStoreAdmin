package com.example.pizzastoreadmint.presentation.order.utils

import androidx.compose.ui.graphics.Color
import com.example.pizzastoreadmint.domain.entity.OrderStatus


fun getStatusColor(orderStatus: OrderStatus): Color {
    return when (orderStatus) {
        OrderStatus.NEW -> Color.Red.copy(alpha = 0.1f)
        OrderStatus.PROCESSING -> Color.LightGray.copy(alpha = 0.1f)
        OrderStatus.FINISH -> Color.Green.copy(alpha = 0.1f)
        OrderStatus.ACCEPT -> Color.Blue.copy(alpha = 0.1f)
    }
}