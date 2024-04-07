package com.example.pizzastoreadmin.presentation.images

sealed class OneImageScreenState {

    object Initial : OneImageScreenState()

    object Loading : OneImageScreenState()

    object Content : OneImageScreenState()
}
