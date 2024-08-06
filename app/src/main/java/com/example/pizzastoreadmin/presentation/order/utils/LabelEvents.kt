package com.example.pizzastoreadmin.presentation.order.utils

sealed interface LabelEvents {

    data object LeaveScreen : LabelEvents
}