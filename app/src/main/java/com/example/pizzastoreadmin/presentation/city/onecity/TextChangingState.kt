package com.example.pizzastoreadmin.presentation.city.onecity

sealed class PointChangingState() {

    data class ChangeAddress(
        val index: Int,
        val address: String
    ) : PointChangingState()

    data class ChangeGeopoint(
        val index: Int,
        val coords: String
    ) : PointChangingState()

    data class DeletePoint(
        val index: Int
    ) : PointChangingState()

    object NewPoint : PointChangingState()
}
