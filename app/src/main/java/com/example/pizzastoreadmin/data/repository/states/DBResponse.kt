package com.example.pizzastoreadmin.data.repository.states

sealed class DBResponse {

    object Processing : DBResponse()

    object Complete : DBResponse()

    data class Error(val description: String) : DBResponse()

}
