package com.example.pizzastoreadmin.data.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pizzastoreadmin.data.localdb.entity.orders.ListOrdersDbModel
import com.example.pizzastoreadmin.data.localdb.entity.products.ListProductsDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PizzaDao {

    @Query("SELECT * FROM orders LIMIT 1")
    fun getOrders(): Flow<ListOrdersDbModel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrders(ordersDbModel: ListOrdersDbModel)

    @Query("SELECT * FROM products LIMIT 1")
    fun getProducts(): Flow<ListProductsDbModel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProducts(productsDbModel: ListProductsDbModel)

    @Query("SELECT * FROM orders LIMIT 1")
    suspend fun getOrdersNoFlow(): ListOrdersDbModel?

    @Query("SELECT * FROM products LIMIT 1")
    suspend fun getProductsOneTime(): ListProductsDbModel?

}