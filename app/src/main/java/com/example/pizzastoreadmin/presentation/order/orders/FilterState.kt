package com.example.pizzastoreadmin.presentation.order.orders

data class FilterState(
    val new: Boolean = false,
    val processing: Boolean = false,
    val finished: Boolean = false,
    val accepted: Boolean = false
)
