package com.example.pizzastoreadmin.data.auth

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface AuthService {

    suspend fun signInWithEmail(email: String, password: String)

    suspend fun signInWithSavedAccounts()

    val authFlow: SharedFlow<AuthResponse>
}