package com.example.pizzastoreadmin.presentation.order.orders

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.domain.usecases.business.GetAllOrdersUseCase
import com.example.pizzastoreadmin.domain.usecases.service.SetCurrentOrderUseCase
import com.example.pizzastoreadmin.presentation.order.orders.OrderListStore.Intent
import com.example.pizzastoreadmin.presentation.order.orders.OrderListStore.Label
import com.example.pizzastoreadmin.presentation.order.orders.OrderListStore.State
import com.example.pizzastoreadmin.presentation.order.sharedstate.FilterState
import kotlinx.coroutines.launch
import javax.inject.Inject

interface OrderListStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class OrderClicked(val order: Order) : Intent

        object StatusClicked : Intent

        object ClickNothing : Intent

        data class StatusItemClicked(val item: OrderStatus) : Intent
    }

    data class State(
        val statusIsExpanded: Boolean = false,
        val filterList: FilterState = FilterState.initFilter(),
        val screenState: OrderListState
    ) {

        sealed interface OrderListState {

            object Loading : OrderListState

            object Error : OrderListState

            object Initial : OrderListState

            data class Content(val orders: List<Order>) : OrderListState

            object LeaveScreen : OrderListState
        }
    }

    sealed interface Label
}

class OrderListStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getOrdersUseCase: GetAllOrdersUseCase,
    private val setCurrentOrderUseCase: SetCurrentOrderUseCase
) {

    fun create(): OrderListStore =
        object : OrderListStore, Store<Intent, State, Label> by storeFactory.create(
            name = "OrderListStore",
            initialState = State(screenState = State.OrderListState.Initial),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        object StartLoading : Action

        object Error : Action

        data class OrdersLoaded(val orders: List<Order>) : Action
    }

    private sealed interface Msg {

        object StartLoading : Msg

        object Error : Msg

        data class OrdersLoaded(val orders: List<Order>) : Msg

        data class StatusItemClicked(val item: OrderStatus) : Msg

        object StatusClicked : Msg

        object ClickNothing : Msg

        object LeaveScreen : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.StartLoading)
            try {
                scope.launch {
                    getOrdersUseCase
                        .getOrdersFlow()
                        .collect {
                            dispatch(Action.OrdersLoaded(it))
                        }

                }
            } catch (e: Exception) {
                dispatch(Action.Error)
            }

        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.OrderClicked -> {
                    setCurrentOrderUseCase.setOrder(intent.order)
                    dispatch(Msg.LeaveScreen)
                }

                Intent.StatusClicked -> {
                    dispatch(Msg.StatusClicked)
                }

                is Intent.StatusItemClicked -> {
                    dispatch(Msg.StatusItemClicked(intent.item))
                }

                Intent.ClickNothing -> {
                    dispatch(Msg.ClickNothing)
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                Action.Error -> {
                    dispatch(Msg.Error)
                }

                is Action.OrdersLoaded -> {
                    dispatch(Msg.OrdersLoaded(action.orders))
                }

                Action.StartLoading -> {
                    dispatch(Msg.StartLoading)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                Msg.Error -> {
                    this.copy(screenState = State.OrderListState.Error)
                }

                is Msg.OrdersLoaded -> {
                    val currentFilter = this.filterList
                    val filteredOrders = msg.orders.filter {
                        !(currentFilter.filterMap[it.status] ?: false)
                    }
                    this.copy(
                        screenState = State.OrderListState.Content(filteredOrders)
                    )
                }

                Msg.StartLoading -> {
                    this.copy(screenState = State.OrderListState.Loading)
                }

                Msg.StatusClicked -> {
                    val currentExpandingFilterStatus = this.statusIsExpanded
                    this.copy(statusIsExpanded = !currentExpandingFilterStatus)
                }

                is Msg.StatusItemClicked -> {
                    val currentFilterStatus = this.filterList.filterMap.toMutableMap()
                    currentFilterStatus[msg.item] = !(currentFilterStatus[msg.item] ?: true)
                    val currentList = if (this.screenState is State.OrderListState.Content) {
                        this.screenState.orders.filter {
                            !(currentFilterStatus[it.status] ?: false)
                        }.sortedByDescending { it.id }
                    } else {
                        null
                    }

                    val screenState = if (currentList != null) {
                        State.OrderListState.Content(currentList)
                    } else {
                        this.screenState
                    }

                    this.copy(
                        filterList = FilterState(currentFilterStatus),
                        screenState = screenState
                    )
                }

                Msg.ClickNothing -> {
                    this.copy(statusIsExpanded = false)
                }

                Msg.LeaveScreen -> {
                    this.copy(screenState = State.OrderListState.LeaveScreen)
                }
            }
    }
}
