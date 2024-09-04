package com.example.pizzastoreadmint.data.localdb.entity.orders

import androidx.room.Entity

@Entity
data class BucketDbModel(
    val order: Map<String, Int> = mapOf()
)
