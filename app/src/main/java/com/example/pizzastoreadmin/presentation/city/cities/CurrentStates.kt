package com.example.pizzastoreadmin.presentation.city.cities

import com.example.pizzastoreadmin.domain.entity.City

data class CurrentStates(
    var citiesToDelete: MutableSet<City>,
    var isCitiesToDeleteEmpty: Boolean,
    var isButtonClicked: Boolean,
    var isItemClicked: Boolean,
    val currentCity: City?
)
