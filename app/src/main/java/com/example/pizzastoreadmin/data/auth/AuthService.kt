package com.example.pizzastoreadmin.data.auth

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface AuthService {

    fun createUser(email: String, password: String): AuthResponse

    suspend fun signInWithEmail(email: String, password: String): AuthResponse

    suspend fun signInWithSavedAccounts()

    val authFlow: StateFlow<AuthResponse>
}