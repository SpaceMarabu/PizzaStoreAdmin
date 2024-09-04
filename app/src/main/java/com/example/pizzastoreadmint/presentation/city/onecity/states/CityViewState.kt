package com.example.pizzastoreadmint.presentation.city.onecity.states

import com.example.pizzastoreadmint.domain.entity.City

data class CityViewState(
    val city: City? = City(),
    val isCityNameIsCorrect: Boolean = true
)
