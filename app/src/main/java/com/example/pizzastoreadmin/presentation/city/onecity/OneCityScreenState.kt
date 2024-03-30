package com.example.pizzastoreadmin.presentation.city.onecity

import com.example.pizzastore.domain.entity.Point

sealed class OneCityScreenState {

    data class ListPoints(val points: List<Point> = listOf()) : OneCityScreenState()
}
