package com.example.pizzastoreadmin.presentation.start

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.PasswordCredential
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastoreadmin.di.getApplicationComponent
import com.example.pizzastore.presentation.start.StartScreenState
import com.example.pizzastore.presentation.start.StartScreenViewModel
import com.example.pizzastoreadmin.presentation.login.LoginScreen
import com.example.pizzastoreadmin.presentation.main.MainScreen
import com.example.pizzastoreadmin.ui.theme.PizzaStoreAdminTheme
import kotlinx.coroutines.coroutineScope

class StartActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val component = getApplicationComponent()
            val viewModel: StartScreenViewModel =
                viewModel(factory = component.getViewModelFactory())

            val cityState = viewModel.state.collectAsState()

//            PizzaStoreAdminTheme {
//
//                when (cityState.value) {
//
//                    StartScreenState.StartScreenContent -> {
//                        MainScreen()
//                    }
//
//                    StartScreenState.Initial -> {
//                        viewModel.changeState(StartScreenState.StartScreenContent)
//                    }
//
//                    else -> {}
//                }
//            }

            LoginScreen()

        }
    }
}
