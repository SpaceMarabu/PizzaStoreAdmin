package com.example.pizzastoreadmin.presentation.city.onecity.states

sealed class OneCityScreenState {

    object Initial : OneCityScreenState()

    object Loading : OneCityScreenState()

    data class Content(
        val city: CityViewState = CityViewState(),
        val points: List<PointViewState> = listOf()
    ) : OneCityScreenState()
}
