package com.example.pizzastoreadmin.presentation.order.oneorder

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.domain.usecases.business.EditOrderUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetCurrentOrderUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.presentation.order.oneorder.OneOrderStore.Intent
import com.example.pizzastoreadmin.presentation.order.oneorder.OneOrderStore.Label
import com.example.pizzastoreadmin.presentation.order.oneorder.OneOrderStore.State
import com.example.pizzastoreadmin.presentation.order.oneorder.OneOrderStore.State.OneOrderState
import kotlinx.coroutines.launch
import javax.inject.Inject

interface OneOrderStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class ChangeStatus(val status: OrderStatus) : Intent

        object StatusClick : Intent

        object DoneClick : Intent

        object StatusClosing : Intent
    }

    data class State(val screenState: OneOrderState) {

        sealed interface OneOrderState {

            object Initial : OneOrderState

            object Loading : OneOrderState

            data class Content(
                val order: Order,
                val isStatusExpanded: Boolean = false
            ) : OneOrderState

            object Error : OneOrderState
        }
    }

    sealed interface Label {

        object EditingSuccess : Label
    }
}

class OneOrderStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getCurrentOrderUseCase: GetCurrentOrderUseCase,
    private val editOrderUseCase: EditOrderUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) {

    fun create(): OneOrderStore =
        object : OneOrderStore, Store<Intent, State, Label> by storeFactory.create(
            name = "OneOrderStore",
            initialState = State(State.OneOrderState.Initial),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        object StartLoading : Action

        data class ContentLoaded(val order: Order) : Action

        object Error : Action

        object SuccessOrderEditing : Action
    }

    private sealed interface Msg {

        object StartLoading : Msg

        data class ContentLoaded(val order: Order) : Msg

        object Error : Msg

        data class ChangeStatus(val status: OrderStatus) : Msg

        object StatusClick : Msg

        object StatusClosing : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            try {
                scope.launch {
                    dispatch(Action.StartLoading)
                    getCurrentOrderUseCase
                        .getOrder()
                        .collect {
                            dispatch(Action.ContentLoaded(it))
                        }
                }
            } catch (e: Exception) {
                dispatch(Action.Error)
            }
            scope.launch {
                getDbResponseUseCase
                    .getDbResponseFlow()
                    .collect {
                        when (it) {
                            DBResponse.Complete -> {
                                dispatch(Action.SuccessOrderEditing)
                            }

                            is DBResponse.Error -> {
                                dispatch(Action.Error)
                            }

                            DBResponse.Processing -> {}
                        }
                    }
            }

        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {

            when (intent) {
                is Intent.ChangeStatus -> {
                    dispatch(Msg.ChangeStatus(intent.status))
                }

                Intent.DoneClick -> {
                    val currentState = getState().screenState
                    if (currentState is OneOrderState.Content) {
                        editOrderUseCase.editOrder(currentState.order)
                    }
                }

                Intent.StatusClick -> {
                    dispatch(Msg.StatusClick)
                }

                Intent.StatusClosing -> {
                    dispatch(Msg.StatusClosing)
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {

            when (action) {
                is Action.ContentLoaded -> {
                    dispatch(Msg.ContentLoaded(action.order))
                }

                Action.Error -> {
                    dispatch(Msg.Error)
                }

                Action.StartLoading -> {
                    dispatch(Msg.StartLoading)
                }

                Action.SuccessOrderEditing -> {
                    publish(Label.EditingSuccess)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.ChangeStatus -> {
                    val currentState = this.screenState
                    if (currentState is OneOrderState.Content) {
                        val newOrder = currentState.order.copy(status = msg.status)
                        this.copy(
                            screenState = currentState.copy(order = newOrder)
                        )
                    } else {
                        this
                    }
                }

                is Msg.ContentLoaded -> {
                    this.copy(screenState = OneOrderState.Content(msg.order))
                }

                Msg.Error -> {
                    this.copy(screenState = OneOrderState.Error)
                }

                Msg.StartLoading -> {
                    this.copy(screenState = OneOrderState.Loading)
                }

                Msg.StatusClick -> {
                    val currentState = this.screenState
                    if (currentState is OneOrderState.Content) {
                        this.copy(
                            screenState = currentState.copy(
                                isStatusExpanded = !currentState.isStatusExpanded
                            )
                        )
                    } else {
                        this
                    }
                }

                Msg.StatusClosing -> {
                    val currentState = this.screenState
                    if (currentState is OneOrderState.Content) {
                        this.copy(
                            screenState = currentState.copy(
                                isStatusExpanded = false
                            )
                        )
                    } else {
                        this
                    }
                }
            }
    }
}
