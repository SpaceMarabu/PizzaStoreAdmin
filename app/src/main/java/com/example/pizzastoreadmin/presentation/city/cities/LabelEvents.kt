package com.example.pizzastoreadmin.presentation.city.cities

sealed interface LabelEvents {

    data object AddOrEditCity: LabelEvents
    data object DeleteComplete: LabelEvents
    data object DeleteFailed: LabelEvents
}