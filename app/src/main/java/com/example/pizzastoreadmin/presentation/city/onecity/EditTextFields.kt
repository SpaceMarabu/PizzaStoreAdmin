package com.example.pizzastoreadmin.presentation.city.onecity

class EditTextFields {

    data class EditPointState(
        val addressCollected: Boolean = false,
        val geopointCollected: Boolean = false
    )

    data class EditCityState(
        val cityCollected: Boolean = false
    )

}
