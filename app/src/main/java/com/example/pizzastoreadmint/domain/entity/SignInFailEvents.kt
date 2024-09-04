package com.example.pizzastoreadmint.domain.entity

sealed interface SignInFailEvents {

    data object NoCredentials: SignInFailEvents

    data object ErrorSignIn: SignInFailEvents

//    data object Success: SignInFailEvents

//    data class Failed(val failReason: FailReason): SignInFailEvents {
//
//        sealed interface FailReason {
//
//            data object NoCredentials: FailReason
//
//            data object ErrorSignIn: FailReason
//        }
//    }
}