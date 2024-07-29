package com.example.pizzastoreadmin.presentation.order.orders

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class OrdersScreenUDFVM @Inject constructor(
    storeFactory: OrderListStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

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