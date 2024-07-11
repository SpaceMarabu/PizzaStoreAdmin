package com.example.pizzastoreadmin.data.mappers

import com.example.pizzastoreadmin.data.localdb.entity.orders.BucketDbModel
import com.example.pizzastoreadmin.data.localdb.entity.orders.OrderDbModel
import com.example.pizzastoreadmin.data.localdb.entity.products.ProductDbModel
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.Bucket
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.entity.ProductType
import javax.inject.Inject

class LocalMapper @Inject constructor() {

    //<editor-fold desc="mapEntityToBucketDbModel">
    fun mapEntityToBucketDbModel(bucket: Bucket): BucketDbModel {
        val productsFromBucket = bucket.order
        val mapProductsForDB = mutableMapOf<String, Int>()
        productsFromBucket.forEach {
            val productId = it.key.id.toString()
            val productCount = it.value
            mapProductsForDB[productId] = productCount
        }
        return BucketDbModel(order = mapProductsForDB)
    }
    //</editor-fold>

    //<editor-fold desc="mapBucketDbModelToEntity">
    private fun mapBucketDbModelToEntity(bucket: BucketDbModel, products: List<Product>): Bucket {
        val orderResult = mutableMapOf<Product, Int>()
        bucket.order.forEach { currentPair ->
            val currentProduct = products.filter { it.id == currentPair.key.toInt() }
            if (currentProduct.isNotEmpty()) {
                orderResult[currentProduct.first()] = currentPair.value
            }
        }
        return Bucket(order = orderResult)
    }
    //</editor-fold>

    //<editor-fold desc="mapOrderDbModelToEntity">
    fun mapOrderDbModelToEntity(orderModel: OrderDbModel?, products: List<Product>): Order? {
        if (orderModel == null) {
            return null
        }
        val id = orderModel.id
        val status: OrderStatus = when (orderModel.status.toInt()) {
            OrderStatus.NEW.ordinal -> OrderStatus.NEW
            OrderStatus.PROCESSING.ordinal -> OrderStatus.PROCESSING
            OrderStatus.FINISH.ordinal -> OrderStatus.FINISH
            else -> OrderStatus.ACCEPT
        }
        val bucket = mapBucketDbModelToEntity(orderModel.bucket, products)
        return Order(
            id = id,
            status = status,
            bucket = bucket
        )
    }
    //</editor-fold>

    //<editor-fold desc="mapOrderToOrderModel">
    fun mapOrderToOrderModel(order: Order) = OrderDbModel(
        id = order.id,
        status = order.status.ordinal.toString(),
        bucket = mapEntityToBucketDbModel(order.bucket)
    )
    //</editor-fold>

    //<editor-fold desc="mapProductToDbModel">
    fun mapProductToDbModel(product: Product): ProductDbModel {
        return ProductDbModel(
            id = product.id,
            type = product.type.type,
            name = product.name,
            price = product.price,
            photo = product.photo,
            description = product.description
        )
    }
    //</editor-fold>

    //<editor-fold desc="dbModelToProduct">
    fun dbModelToProduct(productDbModel: ProductDbModel): Product {
        return Product(
            id = productDbModel.id,
            type = ProductType.fromString(productDbModel.type),
            name = productDbModel.name,
            price = productDbModel.price,
            photo = productDbModel.photo,
            description = productDbModel.description
        )
    }
    //</editor-fold>
}
