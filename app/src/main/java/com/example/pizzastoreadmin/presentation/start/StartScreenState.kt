package com.example.pizzastoreadmin.presentation.start

sealed class StartScreenState() {

    object Initial : StartScreenState()

    object StartScreenContent : StartScreenState()
}
