package com.example.pizzastoreadmin.data.auth

import androidx.credentials.Credential

sealed interface SignInResponse {

    data class Success(val credential: Credential): SignInResponse

    data class Failed(val failReason: FailReason): SignInResponse {

        sealed interface FailReason {

            data object UserCancelled: FailReason

            data object NoCredentials: FailReason
        }
    }
}