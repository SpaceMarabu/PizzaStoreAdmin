package com.example.pizzastoreadmin.presentation.login

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun LoginScreen() {

    val context = LocalContext.current
    val credentialManager by lazy {
        CredentialManager.create(context)
    }

    val web_client_id = "306743262414-psja4h324ubg69kh62l9rso0e09khjfk.apps.googleusercontent.com"

    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(web_client_id)
        .setAutoSelectEnabled(true)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    LaunchedEffect(key1 = Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
//                handleFailure(e)
            }
        }
    }

//    val signInLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            try {
//                val credential =
//                val credential = credentialManager.getSignInCredentialFromIntent(result.data)
//                val idToken = credential.googleIdToken
//                if (idToken != null) {
//                    // Обработка ID токена
//                    Log.d(TAG, "Got ID token.")
//                    onSignInResult(idToken)
//                } else {
//                    // Обработка ситуации, когда токен не был получен
//                    Log.d(TAG, "No ID token!")
//                    onSignInResult(null)
//                }
//            } catch (e: ApiException) {
//                // Обработка исключения
//                Log.e(TAG, "Sign-in failed", e)
//                onSignInResult(null)
//            }
//        }
//    }

    // Пример вызова лаунчера (например, по нажатию кнопки)
//    Button(onClick = {
//        val signInIntent = oneTapClient.signInIntent
//        signInLauncher.launch(signInIntent)
//    }) {
//        Text("Sign In")
//    }

}


fun handleSignIn(result: GetCredentialResponse) {
    // Handle the successfully returned credential.
    when (val credential = result.credential) {

        // Password credential
        is PasswordCredential -> {
            // Send ID and password to your server to validate and authenticate.
            val username = credential.id
            val password = credential.password
        }

        // GoogleIdToken credential
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    // Use googleIdTokenCredential and extract the ID to validate and
                    // authenticate on your server.
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)
                    // You can use the members of googleIdTokenCredential directly for UX
                    // purposes, but don't use them to store or control access to user
                    // data. For that you first need to validate the token:
                    // pass googleIdTokenCredential.getIdToken() to the backend server.
                    googleIdTokenCredential

//                    GoogleIdTokenVerifier verifier = ... // see validation instructions
//                    GoogleIdToken idToken = verifier.verify(idTokenString);

                    // To get a stable account identifier (e.g. for storing user data),
                    // use the subject ID:
//                    idToken.getPayload().getSubject()
                } catch (e: GoogleIdTokenParsingException) {
//                    Log.e(TAG, "Received an invalid google id token response", e)
                }
            } else {
                // Catch any unrecognized custom credential type here.
//                Log.e(TAG, "Unexpected type of credential")
            }
        }

        else -> {
            // Catch any unrecognized credential type here.
//            Log.e(TAG, "Unexpected type of credential")
        }
    }
}

