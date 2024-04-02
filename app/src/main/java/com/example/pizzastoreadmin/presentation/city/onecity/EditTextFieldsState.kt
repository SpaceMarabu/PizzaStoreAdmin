package com.example.pizzastoreadmin.presentation.city.onecity

class EditTextFieldsState {

    data class EditPointState(
        val addressCollected: Boolean = false,
        val geopointCollected: Boolean = false
    )

    data class EditCityState(
        val cityCollected: Boolean = false
    )

    data class EditAllResultState(
        val isPointsCollected: Boolean = false,
        val isCityCollected: Boolean = false,
        val isAllCorrect: Boolean = false
    )

}
