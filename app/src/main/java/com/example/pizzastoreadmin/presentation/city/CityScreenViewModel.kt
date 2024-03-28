package com.example.pizzastoreadmin.presentation.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastore.domain.entity.Point
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.usecases.AddOrEditCItyUseCase
import com.example.pizzastoreadmin.domain.usecases.DeleteCityUseCase
import com.example.pizzastoreadmin.domain.usecases.GetAllCitiesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

class CityScreenViewModel @Inject constructor(
    private val getCitiesUseCase: GetAllCitiesUseCase,
    private val addOrEditCityUseCase: AddOrEditCItyUseCase,
    private val deleteCityUseCase: DeleteCityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CityScreenState>(CityScreenState.Initial)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadCities()
        }
    }

    private fun loadCities() {
        _state.value = CityScreenState.Loading
        viewModelScope.launch {
            getCitiesUseCase
                .getCitiesFlow()
                .filter { it.isNotEmpty() }
                .collect {
                    _state.value = CityScreenState.ListCities(it)
                }
        }
    }

    fun getNewPoint(cities: List<Point>): Point {
        var maxId = 0
        cities.forEach {point ->  if (point.id   > maxId) maxId = point.id }
        return Point(id = maxId + 1)
    }


    private fun addOrEditCity(city: City) {
        addOrEditCityUseCase.addOrEditCity(city)
    }

    fun deleteCity(cities: List<City>) {
        deleteCityUseCase.deleteCity(cities)
    }

    fun changeScreenState(state: CityScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }
}