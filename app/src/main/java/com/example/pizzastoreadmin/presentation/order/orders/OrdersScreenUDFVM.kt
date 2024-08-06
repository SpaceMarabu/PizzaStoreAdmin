package com.example.pizzastoreadmin.presentation.order.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.presentation.order.utils.LabelEvents
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OrdersScreenUDFVM @Inject constructor(
    storeFactory: OrderListStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    private val _labelEvents = MutableSharedFlow<LabelEvents>()
    val labelEvents = _labelEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            store.labels.collect {
                when (it) {
                    OrderListStore.Label.LeaveToOrder -> {
                        _labelEvents.emit(LabelEvents.LeaveScreen)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val model: StateFlow<OrderListStore.State> = store.stateFlow

    fun onStatusClick() = store.accept(OrderListStore.Intent.StatusClicked)

    fun onStatusItemClick(item: OrderStatus) =
        store.accept(
            OrderListStore.Intent.StatusItemClicked(item)
        )

    fun onOrderClick(order: Order) = store.accept(
        OrderListStore.Intent.OrderClicked(order)
    )

    fun onClickNothing() = store.accept(OrderListStore.Intent.ClickNothing)

}