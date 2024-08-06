package com.example.pizzastoreadmin.presentation.order.oneorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.presentation.order.utils.LabelEvents
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OneOrderScreenUDFVM @Inject constructor(
    storeFactory: OneOrderStoreFactory
) : ViewModel(){

    private val store = storeFactory.create()

    val labelEvents = MutableSharedFlow<LabelEvents>()

    init {
        viewModelScope.launch {
            store.labels.collect {
                when(it) {
                    OneOrderStore.Label.EditingSuccess -> {
                        labelEvents.emit(LabelEvents.LeaveScreen)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val model: StateFlow<OneOrderStore.State> = store.stateFlow

    fun onStatusClick() = store.accept(OneOrderStore.Intent.StatusClick)

    fun onStatusItemClick(orderStatus: OrderStatus) =
        store.accept(OneOrderStore.Intent.ChangeStatus(orderStatus))

    fun onDoneClick() = store.accept(OneOrderStore.Intent.DoneClick)

    fun onStatusNeedClose() = store.accept(OneOrderStore.Intent.StatusClosing)

}