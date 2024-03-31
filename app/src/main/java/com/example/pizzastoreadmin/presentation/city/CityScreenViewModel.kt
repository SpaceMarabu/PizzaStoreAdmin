package com.example.pizzastoreadmin.presentation.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastore.domain.entity.Point
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.usecases.AddOrEditCItyUseCase
import com.example.pizzastoreadmin.domain.usecases.DeleteCityUseCase
import com.example.pizzastoreadmin.domain.usecases.GetAllCitiesUseCase
import com.example.pizzastoreadmin.presentation.city.onecity.EditState
import com.example.pizzastoreadmin.presentation.city.onecity.EditType
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityScreenState
import com.example.pizzastoreadmin.presentation.city.onecity.PointState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class CityScreenViewModel @Inject constructor(
    private val getCitiesUseCase: GetAllCitiesUseCase,
    private val addOrEditCityUseCase: AddOrEditCItyUseCase,
    private val deleteCityUseCase: DeleteCityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CityScreenState>(CityScreenState.Initial)
    val state = _state.asStateFlow()

    private val _listPoints = MutableStateFlow(OneCityScreenState.ListPoints())
    val listPoints = _listPoints.asStateFlow()

    private val _needCallback = MutableStateFlow(false)
    val needCallback = _needCallback.asStateFlow()

    private val pointsChanges = MutableSharedFlow<PointState>(
        extraBufferCapacity = 50
    )

    private val resultEditFlow = MutableStateFlow(HashMap<Int, EditState>())

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
                val currentListPoints = getCurrentListPoints().points.toMutableList()
                val resultPoints: List<Point> = when (it) {
                    is PointState.ChangeAddress -> {
                        _needCallback.emit(false)
                        currentListPoints[it.index] =
                            currentListPoints[it.index].copy(address = it.address)
                        //присвоение листу resultPoints
                        currentListPoints
                    }

                    is PointState.ChangeGeopoint -> {
                        _needCallback.emit(false)
                        currentListPoints[it.index] =
                            currentListPoints[it.index].copy(coords = it.coords)
                        //присвоение листу resultPoints
                        currentListPoints
                    }

                    is PointState.Delete -> {
                        if (resultEditFlow.value.values.all { state -> !state.addressCollected && !state.geopointCollected }) {
                            val scope = CoroutineScope(Dispatchers.Default)
                            scope.launch coroutine@{
                                delay(100)
                                pointsChanges.emit(it)
                                return@coroutine
                            }
                            //присвоение листу resultPoints
                            currentListPoints
                        } else {
                            var id = 1
                            var tempListPoints: List<Point> = listOf()
                            currentListPoints.forEachIndexed { index, pointFromList ->
                                if (it.index != index) {
                                    tempListPoints = tempListPoints + pointFromList
                                }
                            }
                            //присвоение листу resultPoints
                            tempListPoints
                        }
                    }

                    PointState.NewPoint -> {
                        var maxId = 0
                        currentListPoints.forEach { point ->
                            if (point.id > maxId) maxId = point.id
                        }
                        //присвоение листу resultPoints
                        currentListPoints + Point(id = maxId + 1)
                    }
                }
                _listPoints.emit(OneCityScreenState.ListPoints(resultPoints))
            }
        }
    }



    private fun emitChange(value: PointState) {
        viewModelScope.launch {
            when (value) {
                is PointState.ChangeAddress -> {
                    val currentPointState = resultEditFlow.value[value.index]
                    if (currentPointState != null) {
                        resultEditFlow.value[value.index] =
                            currentPointState.copy(addressCollected = true)
                    }
                }

                is PointState.ChangeGeopoint -> {
                    val currentPointState = resultEditFlow.value[value.index]
                    if (currentPointState != null) {
                        resultEditFlow.value[value.index] =
                            currentPointState.copy(geopointCollected = true)
                    }
                }

                else -> {}
            }
            pointsChanges.emit(value)
        }
    }

    fun initListPoints(points: List<Point>) {
        viewModelScope.launch {
            _listPoints.emit(OneCityScreenState.ListPoints(points))
        }
    }

    private fun needCallback() {
        viewModelScope.launch {
            //готовлю словарь для проверки, что все изменения полей прилетели
            val sizeListPoints = _listPoints.value.points.size
            val resultMap = HashMap<Int, EditState>()
            for (i in 0 until sizeListPoints) {
                resultMap[i] = EditState()
            }
            resultEditFlow.emit(resultMap)
            _needCallback.emit(true)
        }
    }

    private fun getCurrentListPoints() = _listPoints.value

    fun getNewPoint() {
        emitChange(PointState.NewPoint)
    }

    fun deletePoint(index: Int) {
        needCallback()
        viewModelScope.launch {
            emitChange(PointState.Delete(index))
        }
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