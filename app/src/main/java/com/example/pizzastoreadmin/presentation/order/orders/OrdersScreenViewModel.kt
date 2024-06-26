package com.example.pizzastoreadmin.presentation.order.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.domain.usecases.business.GetAllOrdersUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.domain.usecases.service.SetCurrentOrderUseCase
import com.example.pizzastoreadmin.presentation.product.products.states.WarningState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class OrdersScreenViewModel @Inject constructor(
    private val getOrdersUseCase: GetAllOrdersUseCase,
    private val setCurrentOrderUseCase: SetCurrentOrderUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) : ViewModel() {

    private val _screenState = MutableStateFlow<OrderScreenState>(OrderScreenState.Initial)
    val screenState = _screenState.asStateFlow()

    private val _sourceOrdersFlow = MutableStateFlow<List<Order>>(listOf())

    private val _currentFilterFlow = MutableStateFlow(FilterState.initFilter())
    val currentFilterFlow = _currentFilterFlow.asStateFlow()

    private val _warningState = MutableStateFlow<WarningState>(WarningState.Nothing)
    val warningState = _warningState.asStateFlow()

    init {
        loadProducts()
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

    //<editor-fold desc="loadCities">
    private fun loadProducts() {
        _screenState.value = OrderScreenState.Loading
        viewModelScope.launch {
            getOrdersUseCase
                .getOrdersFlow()
                .filter { it.isNotEmpty() }
                .map { caughtList ->
                    _sourceOrdersFlow.value = caughtList
                    caughtList.filter {
                        val currentFilter = _currentFilterFlow.value
                        !(currentFilter.filterMap[it.status] ?: false)
                    }
                }
                .collect {
                    _screenState.value = OrderScreenState.Content(it)
                }
        }
    }
    //</editor-fold>

    //<editor-fold desc="warningCollected">
    fun warningCollected() {
        viewModelScope.launch {
            _warningState.emit(WarningState.Nothing)
        }
    }
    //</editor-fold>]

    //<editor-fold desc="getOrderStatusDescription">
    fun getOrderStatusDescription(statusIn: OrderStatus) =
        when (statusIn) {
            OrderStatus.NEW -> "NEW"
            OrderStatus.PROCESSING -> "PROCESSING"
            OrderStatus.FINISH -> "FINISH"
            OrderStatus.ACCEPT -> "ACCEPT"
        }
    //</editor-fold>

    //<editor-fold desc="changeFilter">
    fun changeFilter(status: OrderStatus, value: Boolean) {
        val filterStateMap = _currentFilterFlow.value.filterMap.toMutableMap()
        filterStateMap[status] = value
        _currentFilterFlow.value = FilterState(filterStateMap)
        Log.d("TEST_TEST", _currentFilterFlow.value.hashCode().toString())
        val currentFilterFlowValue = _currentFilterFlow.value
        val currentScreenState = _screenState.value
        if (currentScreenState is OrderScreenState.Content) {
            val newOrders = _sourceOrdersFlow.value.filter {
                !(currentFilterFlowValue.filterMap[it.status] ?: false)
            }
            _screenState.value = currentScreenState.copy(orders = newOrders)
        }
    }
    //</editor-fold>

}