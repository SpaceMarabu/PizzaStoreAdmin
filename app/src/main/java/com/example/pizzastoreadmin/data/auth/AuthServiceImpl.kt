package com.example.pizzastoreadmin.data.auth

import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.pizzastore.BuildConfig
import com.example.pizzastoreadmin.di.PizzaStoreAdminApplication
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID

class AuthServiceImpl(
    val application: PizzaStoreAdminApplication
) : AuthService {

    private val credentialManager = CredentialManager.create(application)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authFlow = MutableStateFlow<AuthResponse>(AuthResponse.WaitingRequest)
    override val authFlow: StateFlow<AuthResponse>
        get() = _authFlow.asStateFlow()

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

    override fun createUser(email: String, password: String): AuthResponse {
        TODO("Not yet implemented")
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResponse {


        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                getFirebaseAuthWithSaveCredentials(task)
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
                        _authFlow.emit(AuthResponse.Failed(AuthResponse.Failed.FailReason.UserCancelled))
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

            is GoogleIdTokenCredential -> {

                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdToken.idToken,
                    null
                )

                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        getFirebaseAuthResponse(task)
                    }
            }


            is PasswordCredential -> {
                val passwordCredential = credential as PasswordCredential
                auth.signInWithEmailAndPassword(passwordCredential.id, passwordCredential.password)
                    .addOnCompleteListener { task ->
                        getFirebaseAuthResponse(task)
                    }
            }

            else -> {
                throw IllegalArgumentException("Invalid sign-in method")
            }
        }

    }

    private suspend fun getFirebaseAuthWithSaveCredentials(
        task: Task<AuthResult>,
        credential: Credential
    ) {
        if (getFirebaseAuthResponse(task) && credential is PasswordCredential) {
            credentialManager.createCredential(
                request = CreatePasswordRequest(credential.id, credential.password),
                context = application
            )
        }
    }

    private fun getFirebaseAuthResponse(task: Task<AuthResult>): Boolean {
        if (task.isSuccessful) {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                _authFlow.value = AuthResponse.Success(firebaseUser)
                return true
            }
        } else {
            _authFlow.value =
                AuthResponse.Failed(AuthResponse.Failed.FailReason.ErrorAuth)
            return false
        }
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