package com.example.pizzastoreadmin.presentation.city

import com.example.pizzastoreadmin.domain.entity.City

sealed class CityScreenState() {

    object Initial : CityScreenState()
    object Loading : CityScreenState()

    data class ListCities(
        val cities: List<City>
    ): CityScreenState()

    data class OneCity(
        val city: City
    ): CityScreenState()
}
