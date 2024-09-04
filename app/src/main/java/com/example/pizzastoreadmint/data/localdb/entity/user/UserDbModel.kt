package com.example.pizzastoreadmint.data.localdb.entity.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserDbModel(
    @PrimaryKey
    val id: String,
    val access: Int
)
