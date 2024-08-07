package com.example.pizzastoreadmin.presentation.city.onecity

import com.example.pizzastoreadmin.presentation.city.onecity.OneCityStore.Label

sealed interface LabelEvents {

    data object ErrorRepositoryResponse : LabelEvents

    data object Exit : LabelEvents
}