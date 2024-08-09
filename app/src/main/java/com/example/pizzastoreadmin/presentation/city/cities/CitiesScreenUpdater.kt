package com.example.pizzastoreadmin.presentation.city.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.pizzastoreadmin.domain.entity.City
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CitiesScreenUpdater @Inject constructor(
    storeFactory: CitiesStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    private val _labelEvents = MutableSharedFlow<LabelEvents>()
    val labelEvents = _labelEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            store.labels.collect {
                when (it) {
                    CitiesStore.Label.AddCity -> {
                        _labelEvents.emit(LabelEvents.AddOrEditCity)
                    }
                    CitiesStore.Label.DeleteComplete -> {
                        _labelEvents.emit(LabelEvents.DeleteComplete)
                    }
                    CitiesStore.Label.DeleteFailed -> {
                        _labelEvents.emit(LabelEvents.DeleteFailed)
                    }
                    is CitiesStore.Label.EditCity -> {
                        _labelEvents.emit(LabelEvents.AddOrEditCity)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val model: StateFlow<CitiesStore.State> = store.stateFlow

    fun selectCity(city: City) = store.accept(CitiesStore.Intent.SelectCity(city))

    fun unselectCity(city: City) = store.accept(CitiesStore.Intent.UnselectCity(city))

    fun buttonClick() = store.accept(CitiesStore.Intent.ButtonClick)

    fun cityClick(city: City) = store.accept(CitiesStore.Intent.EditCity(city))
}