package com.example.pizzastoreadmin.data.localdb.entity.orders

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class ListOrdersDbModel (
    @PrimaryKey
    val orders: List<OrderDbModel>
)