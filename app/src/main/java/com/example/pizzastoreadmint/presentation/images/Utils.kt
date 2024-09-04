package com.example.pizzastoreadmint.presentation.images

import com.example.pizzastoreadmint.domain.entity.PictureType
import com.example.pizzastoreadmint.presentation.images.images.PicturesStore.State

fun getAllPictureTypes() = listOf(
    PictureType.PIZZA,
    PictureType.ROLL,
    PictureType.STARTER,
    PictureType.DESSERT,
    PictureType.DRINK,
    PictureType.STORY
)

fun List<Int>.getButtonState() = if (this.isEmpty()) {
    State.ButtonState.Add
} else {
    State.ButtonState.Delete
}