package com.example.pizzastoreadmin.presentation.order.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.domain.usecases.business.GetAllOrdersUseCase
import com.example.pizzastoreadmin.domain.usecases.service.SetCurrentOrderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class OrdersScreenViewModel @Inject constructor(
    private val getOrdersUseCase: GetAllOrdersUseCase,
    private val setCurrentOrderUseCase: SetCurrentOrderUseCase
) : ViewModel() {

    private val _screenState = MutableStateFlow<OrderScreenState>(OrderScreenState.Initial)
    val screenState = _screenState.asStateFlow()

    private val _sourceOrdersFlow = MutableStateFlow<List<Order>>(listOf())

    private val _currentFilterFlow = MutableStateFlow(FilterState.initFilter())
    val currentFilterFlow = _currentFilterFlow.asStateFlow()

    init {
        loadOrders()
    }

    //<editor-fold desc="loadCities">
    private fun loadOrders() {
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
                        .sortedByDescending { it.id  }

                }
                .collect {
                    _screenState.value = OrderScreenState.Content(it)
                }
        }
    }
    //</editor-fold>

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
        val currentFilterFlowValue = _currentFilterFlow.value
        val currentScreenState = _screenState.value
        if (currentScreenState is OrderScreenState.Content) {
            val newOrders = _sourceOrdersFlow.value
                .filter {
                    !(currentFilterFlowValue.filterMap[it.status] ?: false)
                }
                .sortedByDescending { it.id }

            _screenState.value = currentScreenState.copy(orders = newOrders)
        }
    }
    //</editor-fold>

    //<editor-fold desc="setOrder">
    fun setOrder(order: Order) {
        setCurrentOrderUseCase.setOrder(order)
    }
    //</editor-fold>

}