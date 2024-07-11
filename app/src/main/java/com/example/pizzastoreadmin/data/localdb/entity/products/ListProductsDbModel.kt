package com.example.pizzastoreadmin.data.localdb.entity.products

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ListProductsDbModel(
    @PrimaryKey
    val products: List<ProductDbModel>
)