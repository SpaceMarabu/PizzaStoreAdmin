package com.example.pizzastoreadmin.presentation.images.oneimage

sealed class OneImageScreenState {

    object Initial : OneImageScreenState()

    object Loading : OneImageScreenState()

    object Content : OneImageScreenState()
}
