package com.example.pizzastoreadmin.presentation.city.onecity

sealed class ShouldLeaveScreenState {


    object Processing : ShouldLeaveScreenState()

    object Exit : ShouldLeaveScreenState()

    data class Error(
        val description: String
    ) : ShouldLeaveScreenState()
}
