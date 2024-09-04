package com.example.pizzastoreadmint.presentation.images.images

sealed interface LabelEvent {

    data object AddClick : LabelEvent

    data class PictureChosen(val uriString: String) : LabelEvent
}