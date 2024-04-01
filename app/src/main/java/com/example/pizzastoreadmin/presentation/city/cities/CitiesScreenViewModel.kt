package com.example.pizzastoreadmin.presentation.city.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastore.domain.entity.Point
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.usecases.AddOrEditCItyUseCase
import com.example.pizzastoreadmin.domain.usecases.DeleteCityUseCase
import com.example.pizzastoreadmin.domain.usecases.GetAllCitiesUseCase
import com.example.pizzastoreadmin.presentation.city.onecity.EditState
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityScreenState
import com.example.pizzastoreadmin.presentation.city.onecity.TextChangingState
import com.example.pizzastoreadmin.presentation.city.onecity.PointViewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
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

    private val _listPoints = MutableStateFlow(OneCityScreenState.ListPoints())
    val listPoints = _listPoints.asStateFlow()

    private val _needCallback = MutableStateFlow(false)
    val needCallback = _needCallback.asStateFlow()

    private val pointsChanges = MutableSharedFlow<TextChangingState>(
        extraBufferCapacity = 50
    )

    private val resultEditFlow = MutableStateFlow(HashMap<Int, EditState>())

    init {
        loadCities()
        changePointsList()
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

    private fun changePointsList() {
        viewModelScope.launch {
            pointsChanges.collect {

                val currentListPoints = getCurrentListPoints().points.toMutableList()

                val resultPoints: List<PointViewState> = when (it) {
                    is TextChangingState.ChangeAddress -> {

                        val currentPoint = currentListPoints[it.index].point
                        val isAddressCorrect = it.address.isNotBlank()
                        stopCallbackTextedit()
                        currentListPoints[it.index] = currentListPoints[it.index]
                            .copy(
                                point = currentPoint.copy(address = it.address),
                                isAddressValid = isAddressCorrect
                            )

                        //присвоение листу resultPoints
                        currentListPoints

                    }

                    is TextChangingState.ChangeGeopoint -> {

                        val currentPoint = currentListPoints[it.index].point
                        val isGeopointCorrect = it.coords.isNotBlank()
                                && it.coords.matches(Regex("\\d+\\.\\d+\\,\\ ?\\d+\\.\\d+"))

                        stopCallbackTextedit()
                        currentListPoints[it.index] = currentListPoints[it.index]
                            .copy(
                                point = currentPoint.copy(coords = it.coords),
                                isGeopointValid = isGeopointCorrect
                            )

                        //присвоение листу resultPoints
                        currentListPoints

                    }

                    is TextChangingState.DeletePoint -> {

                        if (resultEditFlow.value.values.all {
                                    state -> !state.addressCollected && !state.geopointCollected
                        }) {
                            viewModelScope.launch coroutine@ {
                                delay(100)
                                pointsChanges.emit(it)
                                return@coroutine
                            }
                            //присвоение листу resultPoints
                            currentListPoints
                        } else {
                            var id = 1
                            var tempListPoints: List<PointViewState> = listOf()
                            currentListPoints.forEachIndexed { index, pointFromList ->
                                if (it.index != index) {
                                    tempListPoints = tempListPoints + pointFromList
                                }
                            }

                            //присвоение листу resultPoints
                            tempListPoints
                        }
                    }

                    TextChangingState.NewPoint -> {

                        var maxId = 0
                        currentListPoints.forEach { pointState ->
                            if (pointState.point.id > maxId) maxId = pointState.point.id
                        }

                        //присвоение листу resultPoints
                        currentListPoints + PointViewState(
                            point = Point(
                                id = maxId + 1
                            )
                        )
                    }
                }
                _listPoints.emit(OneCityScreenState.ListPoints(resultPoints))
            }
        }
    }


    private fun emitChange(value: TextChangingState) {
        viewModelScope.launch {
            when (value) {
                is TextChangingState.ChangeAddress -> {
                    val currentPointState = resultEditFlow.value[value.index]
                    if (currentPointState != null) {
                        resultEditFlow.value[value.index] =
                            currentPointState.copy(addressCollected = true)
                    }
                }

                is TextChangingState.ChangeGeopoint -> {
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
            var listPointsState: List<PointViewState> = listOf()
            points.forEach {
                listPointsState = listPointsState + PointViewState(it)
            }
            _listPoints.emit(OneCityScreenState.ListPoints(listPointsState))
        }
    }

    private fun needCallbackTextedit() {
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

    private fun stopCallbackTextedit() {
        viewModelScope.launch {
            _needCallback.emit(false)
        }
    }

    private fun getCurrentListPoints() = _listPoints.value

    fun getNewPoint() {
        emitChange(TextChangingState.NewPoint)
    }

    fun deletePoint(index: Int) {
        needCallbackTextedit()
        viewModelScope.launch {
            emitChange(TextChangingState.DeletePoint(index))
        }
    }

    fun editPoint(
        index: Int,
        address: String? = null,
        coords: String? = null
    ) {
        if (address != null) {
            emitChange(TextChangingState.ChangeAddress(index, address))
        } else if (coords != null) {
            emitChange(TextChangingState.ChangeGeopoint(index, coords))
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