package com.example.pizzastoreadmin.presentation.city.cities.states

import com.example.pizzastoreadmin.domain.entity.City

sealed class CitiesScreenState() {

    object Initial : CitiesScreenState()
    object Loading : CitiesScreenState()

    data class Content(
        val cities: List<City>
    ): CitiesScreenState()

}
