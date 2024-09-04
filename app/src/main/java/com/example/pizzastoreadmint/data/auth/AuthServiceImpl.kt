package com.example.pizzastoreadmint.data.auth

import android.app.Application
import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.pizzastoreadmint.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class AuthServiceImpl(
    val application: Application
) : AuthService {

    private val credentialManager = CredentialManager.create(application)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val savingCredentialContext = CoroutineScope(Dispatchers.Main)
    private val authContext = CoroutineScope(Dispatchers.Main)

    private val _authFlow = MutableSharedFlow<AuthResponse>()
    override val authFlow: SharedFlow<AuthResponse>
        get() = _authFlow.asSharedFlow()

    private val webClientId = BuildConfig.WEB_CLIENT_ID
    private val rawNonce = UUID.randomUUID().toString()
    private val bytes = rawNonce.toByteArray()
    private val md = MessageDigest.getInstance("SHA-256")
    private val digest = md.digest(bytes)
    private val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

    private val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setNonce(hashedNonce)
        .build()

    override suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                getFirebaseAuthWithSaveCredentials(task, PasswordCredential(email, password))
            }
    }

    override suspend fun signInWithSavedAccounts() {

        val googleCredentialResponse = getCredential()
        var credential: Credential? = null

        when (googleCredentialResponse) {
            is SignInResponse.Failed -> {
                when (googleCredentialResponse.failReason) {
                    SignInResponse.Failed.FailReason.NoCredentials -> {
                        _authFlow.emit(AuthResponse.Failed(AuthResponse.Failed.FailReason.NoCredentials))
                        return
                    }

                    SignInResponse.Failed.FailReason.UserCancelled -> {
                        return
                    }
                }
            }

            is SignInResponse.Success -> {
                credential =
                    googleCredentialResponse.credential
            }
        }


        when (credential) {

            is CustomCredential -> {

                val firebaseCredential = try {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                    GoogleAuthProvider.getCredential(
                        googleIdToken.idToken,
                        null
                    )
                } catch (e: Exception) {
                    throw IllegalArgumentException("Invalid sign-in method. ${e.message}")
                }

                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        authContext.launch {
                            getFirebaseAuthResponse(task)
                        }
                    }
            }

            is PasswordCredential -> {
                auth.signInWithEmailAndPassword(credential.id, credential.password)
                    .addOnCompleteListener { task ->
                        authContext.launch {
                            getFirebaseAuthResponse(task)
                        }
                    }
            }

            else -> {
                throw IllegalArgumentException("Invalid sign-in method")
            }
        }

    }

    override suspend fun createUserWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                authContext.launch {
                    getFirebaseAuthWithSaveCredentials(task, PasswordCredential(email, password))
                }
            }
    }

    private fun getFirebaseAuthWithSaveCredentials(
        task: Task<AuthResult>,
        credential: Credential
    ) {
        savingCredentialContext.launch {
            if (getFirebaseAuthResponse(task) && credential is PasswordCredential) {
                credentialManager.createCredential(
                    request = CreatePasswordRequest(credential.id, credential.password),
                    context = application
                )
            }
        }
    }

    private suspend fun getFirebaseAuthResponse(task: Task<AuthResult>): Boolean {
        var result = false
        coroutineScope {
            val deferred = async {
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        _authFlow.emit(AuthResponse.Success(firebaseUser))
                        return@async true
                    }
                    return@async false
                } else {
                    _authFlow.emit(AuthResponse.Failed(AuthResponse.Failed.FailReason.ErrorAuth))
                    return@async false
                }
            }
            try {
                result = deferred.await()
            } catch (e: Exception) {
                _authFlow.emit(AuthResponse.Failed(AuthResponse.Failed.FailReason.ErrorAuth))
            }
        }
        return result
    }

    private suspend fun getCredential(): SignInResponse {
        try {
            val getCredRequest = GetCredentialRequest(
                listOf(GetPasswordOption(), googleIdOption)
            )
            val credentialResponse = credentialManager.getCredential(
                request = getCredRequest,
                context = application,
            )

            return SignInResponse.Success(credentialResponse.credential)
        } catch (e: GetCredentialCancellationException) {
            return SignInResponse.Failed(SignInResponse.Failed.FailReason.UserCancelled)
        } catch (e: NoCredentialException) {
            return SignInResponse.Failed(SignInResponse.Failed.FailReason.UserCancelled)
        } catch (e: GetCredentialException) {
            Log.e("CredentialTest", "Error getting credential", e)
            throw e
        }
    }
}