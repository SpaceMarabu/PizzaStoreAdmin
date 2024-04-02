package com.example.pizzastoreadmin.presentation.city.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.usecases.AddOrEditCItyUseCase
import com.example.pizzastoreadmin.domain.usecases.DeleteCityUseCase
import com.example.pizzastoreadmin.domain.usecases.GetAllCitiesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

class CitiesScreenViewModel @Inject constructor(
    private val getCitiesUseCase: GetAllCitiesUseCase,
    private val addOrEditCityUseCase: AddOrEditCItyUseCase,
    private val deleteCityUseCase: DeleteCityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CitiesScreenState>(CitiesScreenState.Initial)
    val state = _state.asStateFlow()

    init {
        loadCities()
    }

    private fun loadCities() {
        _state.value = CitiesScreenState.Loading
        viewModelScope.launch {
            getCitiesUseCase
                .getCitiesFlow()
                .filter { it.isNotEmpty() }
                .collect {
                    _state.value = CitiesScreenState.ListCities(it)
                }
        }
    }

    private fun addOrEditCity(city: City) {
        addOrEditCityUseCase.addOrEditCity(city)
    }

    fun deleteCity(cities: List<City>) {
        deleteCityUseCase.deleteCity(cities)
    }

    fun changeScreenState(state: CitiesScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }
}