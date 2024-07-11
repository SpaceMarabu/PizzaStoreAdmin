package com.example.pizzastoreadmin.data.localdb

import androidx.room.TypeConverter
import com.example.pizzastoreadmin.data.localdb.entity.orders.BucketDbModel
import com.example.pizzastoreadmin.data.localdb.entity.orders.OrderDbModel
import com.example.pizzastoreadmin.data.localdb.entity.products.ProductDbModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object Converters {

    @TypeConverter
    fun fromOrdersList(orders: List<OrderDbModel?>?): String? {
        if (orders == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<List<OrderDbModel?>?>() {}.type
        return gson.toJson(orders, type)
    }

    @TypeConverter
    fun toOrdersList(orders: String?): List<OrderDbModel?>? {
        if (orders == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<List<OrderDbModel?>?>() {}.type
        return gson.fromJson(orders, type)
    }

    @TypeConverter
    fun orderToEntity(order: OrderDbModel?): String? {
        if (order == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<OrderDbModel?>() {}.type
        return gson.toJson(order, type)
    }

    @TypeConverter
    fun entityToOrder(order: String?): OrderDbModel? {
        if (order == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<OrderDbModel?>() {}.type
        return gson.fromJson(order, type)
    }

    @TypeConverter
    fun bucketToEntity(bucket: BucketDbModel?): String? {
        if (bucket == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<BucketDbModel?>() {}.type
        return gson.toJson(bucket, type)
    }

    @TypeConverter
    fun entityToBucket(product: String?): BucketDbModel? {
        if (product == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<BucketDbModel?>() {}.type
        return gson.fromJson(product, type)
    }

    @TypeConverter
    fun productToEntity(product: ProductDbModel?): String? {
        if (product == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<ProductDbModel?>() {}.type
        return gson.toJson(product, type)
    }

    @TypeConverter
    fun entityToProduct(product: String?): ProductDbModel? {
        if (product == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<ProductDbModel?>() {}.type
        return gson.fromJson(product, type)
    }

    @TypeConverter
    fun fromProductList(products: List<ProductDbModel?>?): String? {
        if (products == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<List<ProductDbModel?>?>() {}.type
        return gson.toJson(products, type)
    }

    @TypeConverter
    fun toProductsList(products: String?): List<ProductDbModel?>? {
        if (products == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<List<ProductDbModel?>?>() {}.type
        return gson.fromJson(products, type)
    }

}