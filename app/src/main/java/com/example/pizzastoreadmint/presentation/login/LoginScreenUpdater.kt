package com.example.pizzastoreadmint.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginScreenUpdater @Inject constructor(
    storeFactory: LoginStoreFactory
) : ViewModel() {

    val store = storeFactory.create()

    private val _labelEvents = MutableSharedFlow<LabelEvent>()
    val labelEvents = _labelEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            store.labels.collect {
                when(it) {
                    is LoginStore.Label.ErrorSignIn -> {
                        _labelEvents.emit(LabelEvent.ErrorSignIn(it.reason))
                    }
                    LoginStore.Label.SuccessSignIn -> {
                        _labelEvents.emit(LabelEvent.ExitScreen)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val model = store.stateFlow

    fun emailSignInClick() = store.accept(LoginStore.Intent.ClickLogin)

    fun credentialSignInClick() = store.accept(LoginStore.Intent.ClickLoginWithSavedAccount)

    fun changeEmail(email: String) = store.accept(LoginStore.Intent.ChangeEmail(email))

    fun changePassword(password: String) = store.accept(LoginStore.Intent.ChangePassword(password))

    fun logOut() = store.accept(LoginStore.Intent.ClickSignOut)

    fun registerClick() = store.accept(LoginStore.Intent.ClickRegister)
}