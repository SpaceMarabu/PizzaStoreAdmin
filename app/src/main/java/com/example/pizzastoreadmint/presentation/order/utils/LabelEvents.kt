package com.example.pizzastoreadmint.presentation.order.utils

sealed interface LabelEvents {

    data object LeaveScreen : LabelEvents
}