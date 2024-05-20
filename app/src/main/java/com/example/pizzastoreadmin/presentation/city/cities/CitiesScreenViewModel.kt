package com.example.pizzastoreadmin.presentation.city.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.usecases.business.DeleteCityUseCase
import com.example.pizzastoreadmin.domain.usecases.business.GetAllCitiesUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.domain.usecases.service.SetCurrentCityUseCase
import com.example.pizzastoreadmin.presentation.city.cities.states.CitiesScreenState
import com.example.pizzastoreadmin.presentation.city.cities.states.WarningState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

class CitiesScreenViewModel @Inject constructor(
    private val getCitiesUseCase: GetAllCitiesUseCase,
    private val deleteCityUseCase: DeleteCityUseCase,
    private val setCurrentCityUseCase: SetCurrentCityUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CitiesScreenState>(CitiesScreenState.Initial)
    val state = _state.asStateFlow()

    private val _warningState = MutableStateFlow<WarningState>(WarningState.Nothing)
    val warningState = _warningState.asStateFlow()



    init {
        loadCities()
        subscribeDbResponse()
    }

    //<editor-fold desc="subscribeDbResponse">
    private fun subscribeDbResponse() {
        viewModelScope.launch {
            getDbResponseUseCase.getDbResponseFlow().collect {
                when (it) {
                    DBResponse.Complete -> {
                        _warningState.emit(WarningState.DeleteComplete())
                    }
                    is DBResponse.Error -> {
                        _warningState.emit(WarningState.DeleteIncomplete())
                    }
                    DBResponse.Processing -> {
                        _warningState.emit(WarningState.Nothing)
                    }
                }
            }
        }
    }
    //</editor-fold>

    private fun loadCities() {
        _state.value = CitiesScreenState.Loading
        viewModelScope.launch {
            getCitiesUseCase
                .getCitiesFlow()
                .filter { it.isNotEmpty() }
                .collect {
                    _state.value = CitiesScreenState.Content(it)
                }
        }
    }

    fun setCurrentCity(city: City? = null) {
        setCurrentCityUseCase.setCity(city)
    }

    fun deleteCity(cities: List<City>) {
        deleteCityUseCase.deleteCity(cities)
    }

    fun warningCollected() {
        viewModelScope.launch {
            _warningState.emit(WarningState.Nothing)
        }
    }
}