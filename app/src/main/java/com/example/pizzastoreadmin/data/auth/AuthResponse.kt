package com.example.pizzastoreadmin.data.auth

import com.google.firebase.auth.FirebaseUser

sealed interface AuthResponse {

    data object WaitingRequest: AuthResponse

    data class Success(val user: FirebaseUser): AuthResponse

    data class Failed(val failReason: FailReason): AuthResponse {

        sealed interface FailReason {

            data object UserCancelled: FailReason

            data object NoCredentials: FailReason

            data object ErrorAuth: FailReason
        }
    }
}