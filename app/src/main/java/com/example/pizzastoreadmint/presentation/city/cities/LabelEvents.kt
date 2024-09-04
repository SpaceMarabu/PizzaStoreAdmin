package com.example.pizzastoreadmint.presentation.city.cities

sealed interface LabelEvents {

    data object AddOrEditCity: LabelEvents
    data object DeleteComplete: LabelEvents
    data object DeleteFailed: LabelEvents
}