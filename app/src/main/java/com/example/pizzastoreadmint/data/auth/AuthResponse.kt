package com.example.pizzastoreadmint.data.auth

import com.google.firebase.auth.FirebaseUser

sealed interface AuthResponse {

    data class Success(val user: FirebaseUser): AuthResponse

    data class Failed(val failReason: FailReason): AuthResponse {

        sealed interface FailReason {

            data object NoCredentials: FailReason

            data object ErrorAuth: FailReason
        }
    }
}