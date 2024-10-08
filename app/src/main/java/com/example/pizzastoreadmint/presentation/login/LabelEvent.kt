package com.example.pizzastoreadmint.presentation.login

sealed interface LabelEvent {

    data class ErrorSignIn(val reason: String) : LabelEvent

    data object ExitScreen : LabelEvent
}