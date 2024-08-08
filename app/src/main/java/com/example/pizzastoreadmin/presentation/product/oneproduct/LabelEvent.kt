package com.example.pizzastoreadmin.presentation.product.oneproduct

sealed interface LabelEvent {

    data object ErrorRepositoryResponse : LabelEvent

    data object PictureClick : LabelEvent

    data object ExitScreen : LabelEvent
}