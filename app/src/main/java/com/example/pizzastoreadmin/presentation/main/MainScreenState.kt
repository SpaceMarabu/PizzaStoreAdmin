package com.example.pizzastoreadmin.presentation.home

sealed class MainScreenState() {

    object Initial : MainScreenState()
    object Loading : MainScreenState()

    object Content : MainScreenState()

}
