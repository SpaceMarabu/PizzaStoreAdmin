package com.example.pizzastoreadmint.presentation.login

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.pizzastoreadmint.domain.entity.SignInFailEvents
import com.example.pizzastoreadmint.domain.entity.UserAccess
import com.example.pizzastoreadmint.domain.usecases.business.SignInUseCase
import com.example.pizzastoreadmint.presentation.login.LoginStore.Intent
import com.example.pizzastoreadmint.presentation.login.LoginStore.Label
import com.example.pizzastoreadmint.presentation.login.LoginStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface LoginStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class ChangeEmail(val email: String) : Intent

        data class ChangePassword(val password: String) : Intent

        data object ClickLogin : Intent

        data object ClickRegister: Intent

        data object ClickLoginWithSavedAccount : Intent

        data object ClickSignOut : Intent
    }

    data class State(
        val email: String = "",
        val password: String = "",
        val contentState: ContentState = ContentState.Initial
    ) {

        sealed interface ContentState {

            data object Initial : ContentState

            data object Content : ContentState

            data object ErrorPermissions : ContentState
        }
    }

    sealed interface Label {

        data object SuccessSignIn : Label

        data class ErrorSignIn(val reason: String) : Label
    }
}

class LoginStoreFactory @Inject constructor(
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

        data object SignInSuccess : Action

        data object ErrorPermissions : Action

        data class SignInFailed(val reason: String) : Action

        data object SignOut : Action
    }

    private sealed interface Msg {

        data class ChangeEmail(val email: String) : Msg

        data class ChangePassword(val password: String) : Msg

        data object ErrorPermissions : Msg

        data object SignOut : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                signInUseCase.getUserUseCase()
                    .collect {
                        if (it != null) {
                            if (it.access == UserAccess.DENIED) {
                                dispatch(Action.ErrorPermissions)
                            } else {
                                dispatch(Action.SignInSuccess)
                            }
                        } else {
                            dispatch(Action.SignOut)
                        }
                    }
            }
            scope.launch {
                signInUseCase.signInEvents.collect { event ->
                    val reasonDescription = when (event) {

                        SignInFailEvents.ErrorSignIn -> "Ошибка в логине или пароле"


                        SignInFailEvents.NoCredentials -> "Аккаунты не найдены"
                    }
                    dispatch(Action.SignInFailed(reasonDescription))
                }
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.ChangeEmail -> {
                    dispatch(Msg.ChangeEmail(intent.email))
                }

                is Intent.ChangePassword -> {
                    dispatch(Msg.ChangePassword(intent.password))
                }

                Intent.ClickLogin -> {
                    val currentState = getState()
                    scope.launch {
                        signInUseCase.signInWithEmail(currentState.email, currentState.password)
                    }
                }

                Intent.ClickLoginWithSavedAccount -> {
                    scope.launch {
                        signInUseCase.signInWithSavedAccounts()
                    }
                }

                Intent.ClickSignOut -> {
                    scope.launch {
                        signInUseCase.signOut()
                    }
                }

                Intent.ClickRegister -> {
                    scope.launch {
                        val currentState = getState()
                        signInUseCase.createUser(currentState.email, currentState.password)
                    }
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                is Action.SignInFailed -> {
                    publish(Label.ErrorSignIn(action.reason))
                }

                Action.SignInSuccess -> {
                    publish(Label.SuccessSignIn)
                }

                Action.ErrorPermissions -> {
                    dispatch(Msg.ErrorPermissions)
                }

                Action.SignOut -> {
                    dispatch(Msg.SignOut)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.ChangeEmail -> {
                    this.copy(email = msg.email)
                }

                is Msg.ChangePassword -> {
                    this.copy(password = msg.password)
                }

                Msg.ErrorPermissions -> {
                    this.copy(contentState = State.ContentState.ErrorPermissions)
                }

                Msg.SignOut -> {
                    this.copy(contentState = State.ContentState.Content)
                }
            }
    }
}
