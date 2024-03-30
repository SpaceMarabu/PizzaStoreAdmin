package com.example.pizzastoreadmin.presentation.city.onecity

sealed class PointState() {

    data class ChangeAddress(
        val index: Int,
        val address: String
    ) : PointState()

    data class ChangeGeopoint(
        val index: Int,
        val coords: String
    ) : PointState()

    data class Delete(
        val index: Int
    ) : PointState()

    object NewPoint : PointState()
}
