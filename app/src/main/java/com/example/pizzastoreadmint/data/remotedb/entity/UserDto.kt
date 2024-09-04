package com.example.pizzastoreadmint.data.remotedb.entity

data class UserDto(
    val id: String = DEFAULT_ID,
    val access: Int = 0
) {
    companion object {
        private const val DEFAULT_ID = "default"
    }
}
