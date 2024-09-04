package com.example.pizzastoreadmint.data.auth

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface AuthService {

    suspend fun signInWithEmail(email: String, password: String)

    suspend fun signInWithSavedAccounts()

    suspend fun createUserWithEmail(email: String, password: String)

    val authFlow: SharedFlow<AuthResponse>
}