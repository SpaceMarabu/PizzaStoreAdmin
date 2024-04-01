package com.example.pizzastoreadmin.presentation.city.onecity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastore.domain.entity.Point
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.usecases.AddOrEditCItyUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OneCityScreenViewModel @Inject constructor(
    private val addOrEditCityUseCase: AddOrEditCItyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<OneCityScreenState>(OneCityScreenState.Initial)
    val state = _state.asStateFlow()

    private val _listPoints: MutableStateFlow<List<PointViewState>> = MutableStateFlow(listOf())
    val listPoints = _listPoints.asStateFlow()

    private val _cityState: MutableStateFlow<CityViewState> = MutableStateFlow(CityViewState())
    val cityState = _cityState.asStateFlow()

    private val _needCallbackPoints = MutableStateFlow(false)
    val needCallbackPoints = _needCallbackPoints.asStateFlow()

    private val _needCallbackCity = MutableStateFlow(false)
    val needCallbackCity = _needCallbackCity.asStateFlow()

    private val pointsChanges = MutableSharedFlow<PointChangingState>(
        extraBufferCapacity = 50
    )

    private val resultEditPointFlow = MutableStateFlow(HashMap<Int, EditTextFields.EditPointState>())
    private val resultEditCityFlow = MutableStateFlow(EditTextFields.EditCityState())

    init {
        changePointsList()
    }

    fun editCityName(cityName: String) {
        viewModelScope.launch {
            _cityState.emit(CityViewState(
                city = City(name = cityName),
                isCityNameIsCorrect = cityName.isNotBlank()
            ))
            resultEditCityFlow.emit(EditTextFields.EditCityState(true))
        }
    }

    private fun changePointsList() {
        viewModelScope.launch {
            pointsChanges.collect {

                val currentListPoints = getCurrentListPoints().toMutableList()

                val resultPoints: List<PointViewState> = when (it) {
                    is PointChangingState.ChangeAddress -> {

                        val currentPoint = currentListPoints[it.index].point
                        val isAddressCorrect = it.address.isNotBlank()
                        stopCallbackPoints()
                        currentListPoints[it.index] = currentListPoints[it.index]
                            .copy(
                                point = currentPoint.copy(address = it.address),
                                isAddressValid = isAddressCorrect
                            )

                        //присвоение листу resultPoints
                        currentListPoints

                    }

                    is PointChangingState.ChangeGeopoint -> {

                        val currentPoint = currentListPoints[it.index].point
                        val isGeopointCorrect = it.coords.isNotBlank()
                                && it.coords.matches(Regex("\\d+\\.\\d+\\,\\ ?\\d+\\.\\d+"))

                        stopCallbackPoints()
                        currentListPoints[it.index] = currentListPoints[it.index]
                            .copy(
                                point = currentPoint.copy(coords = it.coords),
                                isGeopointValid = isGeopointCorrect
                            )

                        //присвоение листу resultPoints
                        currentListPoints

                    }

                    is PointChangingState.DeletePoint -> {

                        if (resultEditPointFlow.value.values.all {
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

                    PointChangingState.NewPoint -> {

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
                _listPoints.emit(resultPoints)
            }
        }
    }


    //<editor-fold desc="emitChangePoints">
    private fun emitChangePoints(value: PointChangingState) {
        viewModelScope.launch {
            when (value) {
                is PointChangingState.ChangeAddress -> {
                    val currentPointState = resultEditPointFlow.value[value.index]
                    if (currentPointState != null) {
                        resultEditPointFlow.value[value.index] =
                            currentPointState.copy(addressCollected = true)
                    }
                }

                is PointChangingState.ChangeGeopoint -> {
                    val currentPointState = resultEditPointFlow.value[value.index]
                    if (currentPointState != null) {
                        resultEditPointFlow.value[value.index] =
                            currentPointState.copy(geopointCollected = true)
                    }
                }

                else -> {}
            }
            pointsChanges.emit(value)
        }
    }
    //</editor-fold>

    //<editor-fold desc="initListPoints">
    fun initListPoints(points: List<Point>) {
        viewModelScope.launch {
            var listPointsState: List<PointViewState> = listOf()
            points.forEach {
                listPointsState = listPointsState + PointViewState(it)
            }
            _listPoints.emit(listPointsState)
        }
    }
    //</editor-fold>

    private fun needCallbackPoints() {
        viewModelScope.launch {
            //готовлю словарь для проверки, что все изменения полей прилетели
            val sizeListPoints = _listPoints.value.size
            val resultMap = HashMap<Int, EditTextFields.EditPointState>()
            for (i in 0 until sizeListPoints) {
                resultMap[i] = EditTextFields.EditPointState()
            }
            resultEditPointFlow.emit(resultMap)
            _needCallbackPoints.emit(true)
        }
    }

    private fun stopCallbackPoints() {
        viewModelScope.launch {
            _needCallbackPoints.emit(false)
        }
    }

    private fun needCallbackCity() {
        viewModelScope.launch {
            resultEditCityFlow.emit(EditTextFields.EditCityState(false))
            _needCallbackCity.emit(true)
        }
    }

    private fun stopCallbackCity() {
        viewModelScope.launch {
            _needCallbackCity.emit(false)
        }
    }

    private fun getCurrentListPoints() = _listPoints.value

    fun getNewPoint() {
        emitChangePoints(PointChangingState.NewPoint)
    }

    fun deletePoint(index: Int) {
        needCallbackPoints()
        viewModelScope.launch {
            emitChangePoints(PointChangingState.DeletePoint(index))
        }
    }

    fun editPoint(
        index: Int,
        address: String? = null,
        coords: String? = null
    ) {
        if (address != null) {
            emitChangePoints(PointChangingState.ChangeAddress(index, address))
        } else if (coords != null) {
            emitChangePoints(PointChangingState.ChangeGeopoint(index, coords))
        }
    }

    private fun addOrEditCity(city: City) {
        addOrEditCityUseCase.addOrEditCity(city)
    }

    fun changeScreenState(state: OneCityScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }
}