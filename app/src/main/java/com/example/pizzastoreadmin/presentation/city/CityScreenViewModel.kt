package com.example.pizzastoreadmin.presentation.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastore.domain.entity.Point
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.usecases.AddOrEditCItyUseCase
import com.example.pizzastoreadmin.domain.usecases.DeleteCityUseCase
import com.example.pizzastoreadmin.domain.usecases.GetAllCitiesUseCase
import com.example.pizzastoreadmin.presentation.city.onecity.PointState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
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

    private val _listPoints = MutableStateFlow<List<Point>>(listOf())
    val listPoints = _listPoints.asStateFlow()

    private val _needCallback = MutableStateFlow(false)
    val needCallback = _needCallback.asStateFlow()

    private val pointsChanges = MutableSharedFlow<PointState>(
        extraBufferCapacity = 50
    )

    init {
        loadCities()
        changePointsList()
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

    private fun changePointsList() {
        viewModelScope.launch {
            pointsChanges.collect {
                val currentListPoints = getCurrentListPoints().toMutableList()
                var resultPoints: List<Point> = listOf()
                when (it) {
                    is PointState.ChangeAddress -> {
                        currentListPoints[it.index] =
                            currentListPoints[it.index].copy(address = it.address)
                        resultPoints = currentListPoints
                    }

                    is PointState.ChangeGeopoint -> {
                        currentListPoints[it.index] =
                            currentListPoints[it.index].copy(address = it.coords)
                        resultPoints = currentListPoints
                    }

                    is PointState.Delete -> {
                        var id = 1
                        currentListPoints.forEachIndexed {index, pointFromList ->
                            if (it.index != index) {
                                resultPoints = resultPoints + pointFromList.copy(id = id++)
                            }
                        }
                    }
                }
                _listPoints.emit(resultPoints)
            }
        }
    }

    private fun emitChange(value: PointState) {
        viewModelScope.launch {
            pointsChanges.emit(value)
        }
    }

    fun initListPoints(points: List<Point>) {
        viewModelScope.launch {
            _listPoints.emit(points)
        }
    }

    fun needCallback() {
        viewModelScope.launch {
            _needCallback.emit(true)
            delay(1000)
            _needCallback.emit(false)
        }
    }

    private fun getCurrentListPoints() = _listPoints.value

    private fun emitListPoints(value: List<Point>) {
        viewModelScope.launch {
            _listPoints.emit(value)
        }
    }

    fun getNewPoint() {
        var maxId = 0
        val currentListPoints = getCurrentListPoints()
        currentListPoints.forEach { point -> if (point.id > maxId) maxId = point.id }
        emitListPoints(currentListPoints + Point(id = maxId + 1))
    }

    fun deletePoint(index: Int) {
        emitChange(PointState.Delete(index))
        needCallback()
//        var resultList: List<Point> = listOf()
//        var id = 1
//        val currentListPoints = getCurrentListPoints()
//        currentListPoints.forEach {
//            if (it.id != point.id) {
//                resultList = resultList + it.copy(id = id++)
//            }
//        }
//        emitListPoints(resultList)
    }

    fun editPoint(
        index: Int,
        address: String? = null,
        coords: String? = null
    ) {
        if (address != null) {
            emitChange(PointState.ChangeAddress(index, address))
        } else if (coords != null) {
            emitChange(PointState.ChangeGeopoint(index, coords))
        }
//        val currentListPoints = getCurrentListPoints()
//        var resultList: List<Point> = listOf()
//        currentListPoints.forEach {
//            resultList = if (it.id == point.id) {
//                val newPoint =
//                    it.copy(address = address ?: it.address, coords = coords ?: it.coords)
//                resultList + newPoint
//            } else {
//                resultList + it
//            }
//        }
//        resultList
//        emitListPoints(resultList)
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