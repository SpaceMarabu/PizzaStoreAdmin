package com.example.pizzastoreadmin.presentation.order.orders

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

    private val _currentFilterFlow = MutableStateFlow<List<OrderStatus>>(listOf())
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
                    caughtList.filter {
                        it.status !in _currentFilterFlow.value
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
    fun changeFilter(filter: List<OrderStatus>) {
        _currentFilterFlow.value = filter
        val currentScreenState = _screenState.value
        if (currentScreenState is OrderScreenState.Content) {
            val newOrders = currentScreenState.orders.filter {
                it.status !in filter
            }
            _screenState.value = currentScreenState.copy(orders = newOrders)
        }
    }
    //</editor-fold>

}