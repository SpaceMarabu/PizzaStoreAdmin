package com.example.pizzastoreadmin.presentation.order.oneorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.domain.usecases.business.EditOrderUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetCurrentOrderUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.presentation.sharedstates.ShouldLeaveScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OneOrderScreenViewModel @Inject constructor(
    private val getCurrentOrderUseCase: GetCurrentOrderUseCase,
    private val editOrderUseCase: EditOrderUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) : ViewModel() {

    private val _screenState = MutableStateFlow<OneOrderScreenState>(OneOrderScreenState.Initial)
    val screenState = _screenState.asStateFlow()

    private val _shouldLeaveScreenState: MutableStateFlow<ShouldLeaveScreenState> =
        MutableStateFlow(ShouldLeaveScreenState.Processing)
    val shouldLeaveScreenState = _shouldLeaveScreenState.asStateFlow()

    init {
        getOrder()
        subscribeDbResponse()
    }

    //<editor-fold desc="getOrder">
    private fun getOrder() {
        _screenState.value = OneOrderScreenState.Loading
        viewModelScope.launch {
            getCurrentOrderUseCase
                .getOrder()
                .collect {
                    if (it != null) {
                        _screenState.value = OneOrderScreenState.Content(it)
                    }
                }
        }
    }
    //</editor-fold>

    //<editor-fold desc="editOrderStatus">
    fun editOrderStatus(status: OrderStatus) {
        val currentScreenStateValue = _screenState.value
        if (currentScreenStateValue is OneOrderScreenState.Content) {
            val order = currentScreenStateValue.order.copy(status = status)
            editOrderUseCase.editOrder(order)
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

}