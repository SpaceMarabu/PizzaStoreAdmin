package com.example.pizzastoreadmin.presentation.login

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.pizzastoreadmin.domain.usecases.business.SignInUseCase
import com.example.pizzastoreadmin.presentation.login.LoginStore.Intent
import com.example.pizzastoreadmin.presentation.login.LoginStore.Label
import com.example.pizzastoreadmin.presentation.login.LoginStore.State

internal interface LoginStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class ChangeEmail(val email: String): Intent

        data class ChangePassword(val password: String): Intent

        data object ClickLogin: Intent

        data object ClickLoginWithSavedAccount: Intent
    }

    data class State(
        val contentState: ContentState = ContentState.Initial
    ) {

        sealed interface ContentState {

            data object Initial: ContentState

            data object Content: ContentState
        }
    }

    sealed interface Label {

        data object SuccessLogin: Label

        data class ErrorLogin(val reason: String): Label
    }
}

internal class LoginStoreFactory(
    private val storeFactory: StoreFactory,
    private val signInUseCase: SignInUseCase
) {

    fun create(): LoginStore =
        object : LoginStore, Store<Intent, State, Label> by storeFactory.create(
            name = "LoginStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {


    }

    private sealed interface Msg {
    }

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
        }
    }

    private class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
        }

        override fun executeAction(action: Action, getState: () -> State) {
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(message: Msg): State =
            when (message) {
            }
    }
}
