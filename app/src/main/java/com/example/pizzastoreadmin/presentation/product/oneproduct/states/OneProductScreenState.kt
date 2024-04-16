package com.example.pizzastoreadmin.presentation.product.oneproduct.states

sealed class OneProductScreenState {

    object Initial : OneProductScreenState()

    object Loading : OneProductScreenState()

    object Content : OneProductScreenState()
}
