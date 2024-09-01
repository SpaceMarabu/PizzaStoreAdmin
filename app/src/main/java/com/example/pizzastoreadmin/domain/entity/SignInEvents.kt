package com.example.pizzastoreadmin.domain.entity

import com.google.firebase.auth.FirebaseUser

sealed interface SignInEvents {

    data object Success: SignInEvents

    data class Failed(val failReason: FailReason): SignInEvents {

        sealed interface FailReason {

            data object UserCancelled: FailReason

            data object NoCredentials: FailReason

            data object ErrorSignIn: FailReason
        }
    }
}