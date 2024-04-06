package com.example.pizzastoreadmin.presentation.city.onecity.states

import com.example.pizzastoreadmin.domain.entity.City

data class CityViewState(
    val city: City? = City(),
    val isCityNameIsCorrect: Boolean = true
)
