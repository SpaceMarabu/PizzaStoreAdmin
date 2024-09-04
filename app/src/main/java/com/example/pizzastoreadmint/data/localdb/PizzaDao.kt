package com.example.pizzastoreadmint.data.localdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pizzastoreadmint.data.localdb.entity.orders.OrderDbModel
import com.example.pizzastoreadmint.data.localdb.entity.products.ProductDbModel
import com.example.pizzastoreadmint.data.localdb.entity.user.UserDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PizzaDao {

    @Query("SELECT * FROM orders LIMIT 1")
    fun getOrders(): Flow<List<OrderDbModel>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrders(ordersDbModel: List<OrderDbModel>)

    @Query("SELECT * FROM products LIMIT 1")
    fun getProducts(): Flow<List<ProductDbModel>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProducts(productsDbModel: List<ProductDbModel>)

    @Query("SELECT * FROM orders LIMIT 1")
    suspend fun getOrdersNoFlow(): List<OrderDbModel>?

    @Query("SELECT * FROM products LIMIT 1")
    suspend fun getProductsOneTime(): List<ProductDbModel>?

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): UserDbModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putUser(user: UserDbModel)

    @Delete
    suspend fun deleteUser(user: UserDbModel)

}