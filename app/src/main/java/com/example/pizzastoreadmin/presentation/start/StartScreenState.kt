package com.example.pizzastore.presentation.start

sealed class StartScreenState() {

    object Initial : StartScreenState()

    object StartScreenContent : StartScreenState()
}
