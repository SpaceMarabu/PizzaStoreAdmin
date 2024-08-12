package com.example.pizzastoreadmin.presentation.images

import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.presentation.images.images.PicturesStore.State

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