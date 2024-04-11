package com.example.pizzastoreadmin.presentation.images.images

sealed class ImagesScreenState {

    object Initial : ImagesScreenState()

    object Loading : ImagesScreenState()

    object Content : ImagesScreenState()
}
