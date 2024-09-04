package com.example.pizzastoreadmint.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastoreadmint.R
import com.example.pizzastoreadmint.di.getApplicationComponent
import com.example.pizzastoreadmint.presentation.funs.getOutlinedColors
import com.example.pizzastoreadmint.presentation.funs.showToastWarn


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
                onSavedAccountsClick = { updater.credentialSignInClick() },
                registerClick = { updater.registerClick() }
            )
        }

        LoginStore.State.ContentState.ErrorPermissions -> {
            ErrorPermissionsScreen {
                updater.logOut()
            }
        }

        LoginStore.State.ContentState.Initial -> {}
    }
}

//<editor-fold desc="ScreenContent">
@Composable
private fun ScreenContent(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailSignInClick: () -> Unit,
    onSavedAccountsClick: () -> Unit,
    registerClick: () -> Unit
) {

    Column {
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally),
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_admin),
            contentDescription = null
        )
        SignInTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = 16.dp
                ),
            label = "Login",
            text = email,
            onValueChange = {
                onEmailChange(it)
            }
        )
        SignInTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    end = 8.dp
                ),
            label = "Password",
            text = password,
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = {
                onPasswordChange(it)
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 32.dp)
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.tertiary)
                .clickable { onEmailSignInClick() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sign In",
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 16.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 32.dp)
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.tertiary)
                .clickable { onSavedAccountsClick() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sign In With Passkey",
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 16.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 32.dp)
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .clickable { registerClick() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Register With Email",
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
//</editor-fold>

//<editor-fold desc="SignInTextField">
@Composable
private fun SignInTextField(
    modifier: Modifier,
    label: String,
    text: String,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        modifier = modifier,
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
//</editor-fold>

//<editor-fold desc="ErrorPermissionsScreen">
@Composable
private fun ErrorPermissionsScreen(
    logOut: () -> Unit
) {
    Column {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Аккаунт не имеет доступа в приложение",
                fontSize = 16.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 32.dp)
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.tertiary)
                .clickable { logOut() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Log Out",
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
//</editor-fold>


