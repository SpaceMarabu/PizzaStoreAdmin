package com.example.pizzastoreadmin.presentation.product.oneproduct.states

data class EditTextFieldState(
    val isNameCollected: Boolean = false,
    val isPriceCollected: Boolean = false,
    val isTypeCollected: Boolean = false,
    val isPhotoCollected: Boolean = false,
    val isDescriptionCollected: Boolean = false
)
