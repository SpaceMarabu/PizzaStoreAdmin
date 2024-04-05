package com.example.pizzastoreadmin.presentation.city.onecity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastore.domain.entity.Point
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.usecases.AddOrEditCItyUseCase
import com.example.pizzastoreadmin.domain.usecases.GetCurrentCityUseCase
import com.example.pizzastoreadmin.domain.usecases.GetDbResponseUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OneCityScreenViewModel @Inject constructor(
    private val addOrEditCityUseCase: AddOrEditCItyUseCase,
    private val getCurrentCityUseCase: GetCurrentCityUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<OneCityScreenState>(OneCityScreenState.Initial)
    val state = _state.asStateFlow()

    private val _listPoints: MutableStateFlow<List<PointViewState>> = MutableStateFlow(listOf())
    val listPoints = _listPoints.asStateFlow()

    private val _cityState: MutableStateFlow<CityViewState> = MutableStateFlow(CityViewState())
    val cityState = _cityState.asStateFlow()

    private val _needCallback = MutableStateFlow(false)
    val needCallback = _needCallback.asStateFlow()

    private val _shouldLeaveScreenState: MutableStateFlow<ShouldLeaveScreenState> =
        MutableStateFlow(ShouldLeaveScreenState.Processing)
    val shouldLeaveScreenState = _shouldLeaveScreenState.asStateFlow()

    private val _screenChanges = MutableSharedFlow<ScreenChangingState>(
        extraBufferCapacity = 50
    )

    private val resultEditPointFlow =
        MutableStateFlow(HashMap<Int, EditTextFieldsState.EditPointState>())
    private val resultEditCityFlow = MutableStateFlow(EditTextFieldsState.EditCityState())
    private val resultEditAll = MutableStateFlow(EditTextFieldsState.EditAllResultState())

    init {
        getCurrentCity()
        changeScreenState(OneCityScreenState.Content())
        changeScreenContent()
        subscribeDbResponse()
    }

    //<editor-fold desc="getCurrentCity">
    private fun getCurrentCity() {
        viewModelScope.launch {
            getCurrentCityUseCase.getCity().collect {
                _cityState.value = CityViewState(city = it)
                initListPoints(it.points)
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="subscribeDbResponse">
    private fun subscribeDbResponse() {
        viewModelScope.launch {
            getDbResponseUseCase.getDbResponseFlow().collect {
                when (it) {
                    DBResponse.Complete -> {
                        _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Exit)
                    }
                    is DBResponse.Error -> {
                        _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Error(it.description))
                    }
                    DBResponse.Processing -> {
                        _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Processing)
                    }
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="changeScreenContent">
    private fun changeScreenContent() {
        viewModelScope.launch {
            _screenChanges.collect {

                val currentListPoints = getCurrentListPoints().toMutableList()
                var resultPoints: List<PointViewState> = listOf()

                when (val currentChangingState = it) {
                    is ScreenChangingState.ChangeAddress -> {

                        val currentPoint = currentListPoints[currentChangingState.index].point
                        val isAddressCorrect = currentChangingState.address.isNotBlank()
                        currentListPoints[currentChangingState.index] =
                            currentListPoints[currentChangingState.index]
                                .copy(
                                    point = currentPoint.copy(address = currentChangingState.address),
                                    isAddressValid = isAddressCorrect
                                )

                        //присвоение листу resultPoints
                        resultPoints = currentListPoints

                    }

                    is ScreenChangingState.ChangeGeopoint -> {

                        val currentPoint = currentListPoints[currentChangingState.index].point
                        val regex = Regex("\\d+\\.\\d+\\,\\ ?\\d+\\.\\d+")

                        val isGeopointCorrect = currentChangingState.coords.isNotBlank()
                                && currentChangingState.coords.trim().matches(regex)

                        currentListPoints[currentChangingState.index] =
                            currentListPoints[currentChangingState.index]
                                .copy(
                                    point = currentPoint.copy(
                                        coords = currentChangingState.coords.trim()
                                    ),
                                    isGeopointValid = isGeopointCorrect
                                )

                        //присвоение листу resultPoints
                        resultPoints = currentListPoints

                    }

                    is ScreenChangingState.DeletePoint -> {

                        if (checkAllPointsCollected()) {
                            emitChangeBackToFlow(currentChangingState)

                            //присвоение листу resultPoints
                            resultPoints = currentListPoints
                        } else {
                            var id = 1
                            currentListPoints.forEachIndexed { index, pointFromList ->
                                if (currentChangingState.index != index) {
                                    //присвоение листу resultPoints
                                    resultPoints = resultPoints + pointFromList
                                }
                            }
                        }
                    }

                    ScreenChangingState.NewPoint -> {

                        var maxId = 0
                        currentListPoints.forEach { pointState ->
                            if (pointState.point.id > maxId) maxId = pointState.point.id
                        }

                        //присвоение листу resultPoints
                        resultPoints = currentListPoints + PointViewState(
                            point = Point(
                                id = maxId + 1
                            )
                        )
                    }

                    is ScreenChangingState.ChangeCityName -> {
                        if (checkAllPointsCollected()) {
                            val collectedCityName = currentChangingState.cityName
                            val isCityNameCorrect = collectedCityName.isNotBlank()
                            val currentCityFromState = _cityState.value.city ?: City()
                            var pointList = listOf<Point>()
                            currentListPoints.forEach { pointViewState ->
                                pointList = pointList + pointViewState.point
                            }
                            _cityState.emit(
                                CityViewState(
                                    city = currentCityFromState.copy(
                                        name = collectedCityName,
                                        points = pointList
                                    ),
                                    isCityNameIsCorrect = isCityNameCorrect
                                )
                            )
                            resultEditCityFlow.emit(EditTextFieldsState.EditCityState(true))
                        } else {
                            emitChangeBackToFlow(currentChangingState)
                        }
                        resultPoints = currentListPoints
                        stopCallbackScreen()
                    }

                    ScreenChangingState.Return -> {
                        if (checkCitynameCollected() && checkCorrectFields()) {
                            val currentCity = _cityState.value.city
                            if (currentCity != null) {
                                addOrEditCityUseCase.addOrEditCity(currentCity)
                            }
                        } else {
                            emitChangeBackToFlow(currentChangingState)
                        }
                        resultPoints = currentListPoints
                    }
                }
                _listPoints.emit(resultPoints)
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="exitScreen">
    fun exitScreen() {
        viewModelScope.launch {
            needCallbackScreen()
            _screenChanges.emit(ScreenChangingState.Return)
            _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Processing)
        }
    }
    //</editor-fold>

    //<editor-fold desc="emitChangeBackToFlow">
    private fun emitChangeBackToFlow(value: ScreenChangingState) {
        viewModelScope.launch coroutine@{
            delay(100)
            _screenChanges.emit(value)
            return@coroutine
        }
    }
    //</editor-fold>

    //<editor-fold desc="checkCorrectFields">
    private fun checkCorrectFields(): Boolean {
        return _listPoints.value.all { it.isGeopointValid && it.isAddressValid }
                && _cityState.value.isCityNameIsCorrect
    }
    //</editor-fold>

    //<editor-fold desc="checkCitynameCollected">
    private fun checkCitynameCollected(): Boolean {
        return resultEditCityFlow.value.cityCollected
    }
    //</editor-fold>

    //<editor-fold desc="checkAllPointsCollected">
    private fun checkAllPointsCollected(): Boolean {
        return (resultEditPointFlow.value.values.all { state ->
            state.addressCollected && state.geopointCollected
        })
    }
    //</editor-fold>

    //<editor-fold desc="editCityName">
    fun editCityName(cityName: String) {
        viewModelScope.launch {
            _screenChanges.emit(ScreenChangingState.ChangeCityName(cityName))
        }
    }
    //</editor-fold>

    //<editor-fold desc="emitChangePoints">
    private fun emitChangePoints(value: ScreenChangingState) {
        viewModelScope.launch {
            when (value) {
                is ScreenChangingState.ChangeAddress -> {
                    val currentPointState = resultEditPointFlow.value[value.index]
                    if (currentPointState != null) {
                        resultEditPointFlow.value[value.index] =
                            currentPointState.copy(addressCollected = true)
                    }
                }

                is ScreenChangingState.ChangeGeopoint -> {
                    val currentPointState = resultEditPointFlow.value[value.index]
                    if (currentPointState != null) {
                        resultEditPointFlow.value[value.index] =
                            currentPointState.copy(geopointCollected = true)
                    }
                }

                else -> {}
            }
            _screenChanges.emit(value)
        }
    }
    //</editor-fold>

    //<editor-fold desc="initListPoints">
    private fun initListPoints(points: List<Point>?) {
        viewModelScope.launch {
            var listPointsState: List<PointViewState> = listOf()
            points?.forEach {
                listPointsState = listPointsState + PointViewState(it)
            }
            _listPoints.emit(listPointsState)
        }
    }
    //</editor-fold>

    //<editor-fold desc="needCallbackScreen">
    private fun needCallbackScreen() {
        viewModelScope.launch {
            //готовлю словарь для проверки, что все изменения полей прилетели
            val sizeListPoints = _listPoints.value.size
            val resultMap = HashMap<Int, EditTextFieldsState.EditPointState>()
            for (i in 0 until sizeListPoints) {
                resultMap[i] = EditTextFieldsState.EditPointState()
            }
            resultEditPointFlow.emit(resultMap)
            resultEditCityFlow.emit(EditTextFieldsState.EditCityState())
            resultEditAll.emit(EditTextFieldsState.EditAllResultState())
            _needCallback.emit(true)
        }
    }
    //</editor-fold>

    //<editor-fold desc="stopCallbackScreen">
    private fun stopCallbackScreen() {
        viewModelScope.launch {
            _needCallback.emit(false)
        }
    }
    //</editor-fold>

    private fun getCurrentListPoints() = _listPoints.value

    fun getNewPoint() {
        emitChangePoints(ScreenChangingState.NewPoint)
    }

    //<editor-fold desc="deletePoint">
    fun deletePoint(index: Int) {
        needCallbackScreen()
        viewModelScope.launch {
            emitChangePoints(ScreenChangingState.DeletePoint(index))
        }
    }
    //</editor-fold>

    //<editor-fold desc="editPoint">
    fun editPoint(
        index: Int,
        address: String? = null,
        coords: String? = null
    ) {
        if (address != null) {
            emitChangePoints(ScreenChangingState.ChangeAddress(index, address))
        } else if (coords != null) {
            emitChangePoints(ScreenChangingState.ChangeGeopoint(index, coords))
        }
    }
    //</editor-fold>

    private fun changeScreenState(state: OneCityScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }
}