package com.example.pizzastoreadmin.presentation.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastoreadmin.di.getApplicationComponent
import com.example.pizzastoreadmin.presentation.funs.getOutlinedColors
import com.example.pizzastoreadmin.presentation.funs.showToastWarn


@Composable
fun LoginScreen(
    exitScreen: () -> Unit
) {

    val component = getApplicationComponent()
    val updater: LoginScreenUpdater = viewModel(factory = component.getViewModelFactory())

    val model by updater.model.collectAsState()

    val currentContext = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        updater.labelEvents.collect {
            when (it) {

                is LabelEvent.ErrorSignIn -> {
                    showToastWarn(currentContext, it.reason)
                }

                LabelEvent.ExitScreen -> {
                    exitScreen()
                }
            }
        }
    }

    when (model.contentState) {
        LoginStore.State.ContentState.Content -> {
            ScreenContent(
                email = model.email,
                password = model.password,
                onEmailChange = { updater.changeEmail(it) },
                onPasswordChange = { updater.changePassword(it) },
                onEmailSignInClick = { updater.emailSignInClick() },
                onSavedAccountsClick = { updater.credentialSignInClick() }
            )
        }

        LoginStore.State.ContentState.ErrorPermissions -> {

        }

        LoginStore.State.ContentState.Initial -> { }
    }
}

@Composable
private fun ScreenContent(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailSignInClick: () -> Unit,
    onSavedAccountsClick: () -> Unit
) {

    Column {
        Spacer(modifier = Modifier.weight(1f))
        SignInTextField(
            label = "Login",
            text = email,
            onValueChange = {
                onEmailChange(it)
            }
        )
        SignInTextField(
            label = "Password",
            text = password,
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = {
                onPasswordChange(it)
            }
        )
        Button(onClick = { onEmailSignInClick() }) {
            Text(text = "Sign In")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { onSavedAccountsClick() }) {
            Text(text = "Sign In With Passkey")
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SignInTextField(
    label: String,
    text: String,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp
            ),
        label = { Text(text = label) },
        value = text,
        onValueChange = {
            onValueChange(it)
        },
        colors = getOutlinedColors(),
        shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp)),
        visualTransformation = visualTransformation
    )
}


