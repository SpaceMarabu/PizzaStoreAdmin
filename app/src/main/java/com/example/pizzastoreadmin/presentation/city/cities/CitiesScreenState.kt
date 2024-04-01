package com.example.pizzastoreadmin.presentation.city.cities

import com.example.pizzastoreadmin.domain.entity.City

sealed class CitiesScreenState() {

    object Initial : CitiesScreenState()
    object Loading : CitiesScreenState()

    data class ListCities(
        val cities: List<City>
    ): CitiesScreenState()

}
