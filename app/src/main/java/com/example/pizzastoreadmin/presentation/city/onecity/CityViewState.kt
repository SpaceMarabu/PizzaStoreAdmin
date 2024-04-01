package com.example.pizzastoreadmin.presentation.city.onecity

import com.example.pizzastoreadmin.domain.entity.City

data class CityViewState(
    val city: City? = null,
    val isCityNameIsCorrect: Boolean = true
)
