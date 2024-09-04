package com.example.pizzastoreadmint.presentation.city.onecity.states


sealed class ScreenChangingState() {

    data class ChangeCityName(
        val cityName: String
    ) : ScreenChangingState()

    data class ChangeAddress(
        val index: Int,
        val address: String
    ) : ScreenChangingState()

    data class ChangeGeopoint(
        val index: Int,
        val coords: String
    ) : ScreenChangingState()

    data class DeletePoint(
        val index: Int
    ) : ScreenChangingState()

    object NewPoint : ScreenChangingState()

    object Return : ScreenChangingState()
}
