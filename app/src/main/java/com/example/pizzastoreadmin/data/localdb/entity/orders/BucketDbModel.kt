package com.example.pizzastoreadmin.data.localdb.entity.orders

import androidx.room.Entity

@Entity
data class BucketDbModel(
    val order: Map<String, Int> = mapOf()
)
