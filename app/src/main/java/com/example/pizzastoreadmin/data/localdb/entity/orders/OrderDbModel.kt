package com.example.pizzastoreadmin.data.localdb.entity.orders

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderDbModel(
    @PrimaryKey
    val id: Int,
    val status: String,
    val bucket: BucketDbModel
)
