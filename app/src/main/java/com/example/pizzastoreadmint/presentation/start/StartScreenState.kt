package com.example.pizzastoreadmint.presentation.start

sealed class StartScreenState() {

    object Initial : StartScreenState()

    object StartScreenContent : StartScreenState()
}
