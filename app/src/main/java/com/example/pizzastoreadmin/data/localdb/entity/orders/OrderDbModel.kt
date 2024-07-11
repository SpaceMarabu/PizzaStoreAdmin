package com.example.pizzastoreadmin.data.localdb.entity.orders

import androidx.room.Entity

@Entity
data class OrderDbModel(
    val id: Int,
    val status: String,
    val bucket: BucketDbModel
)
