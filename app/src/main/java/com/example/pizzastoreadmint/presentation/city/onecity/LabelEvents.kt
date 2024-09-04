package com.example.pizzastoreadmint.presentation.city.onecity

import com.example.pizzastoreadmint.presentation.city.onecity.OneCityStore.Label

sealed interface LabelEvents {

    data object ErrorRepositoryResponse : LabelEvents

    data object Exit : LabelEvents
}