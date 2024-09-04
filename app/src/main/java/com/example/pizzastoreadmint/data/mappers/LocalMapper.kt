package com.example.pizzastoreadmint.data.mappers

import com.example.pizzastoreadmint.data.localdb.entity.orders.BucketDbModel
import com.example.pizzastoreadmint.data.localdb.entity.orders.OrderDbModel
import com.example.pizzastoreadmint.data.localdb.entity.products.ProductDbModel
import com.example.pizzastoreadmint.data.localdb.entity.user.UserDbModel
import com.example.pizzastoreadmint.domain.entity.Order
import com.example.pizzastoreadmint.domain.entity.Bucket
import com.example.pizzastoreadmint.domain.entity.OrderStatus
import com.example.pizzastoreadmint.domain.entity.Product
import com.example.pizzastoreadmint.domain.entity.ProductType
import com.example.pizzastoreadmint.domain.entity.User
import com.example.pizzastoreadmint.domain.entity.UserAccess
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

    //<editor-fold desc="mapUserToDbModel">
    fun mapUserToDbModel(user: User) = UserDbModel(
        id = user.id,
        access = user.access.ordinal
    )
    //</editor-fold>

    //<editor-fold desc="mapDbModelToUser">
    fun mapDbModelToUser(model: UserDbModel) = User(
        id = model.id,
        access = when (model.access) {
            1 -> UserAccess.GRANTED
            else -> UserAccess.DENIED
        }
    )
    //</editor-fold>
}
