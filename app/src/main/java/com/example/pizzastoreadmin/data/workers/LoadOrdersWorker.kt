package com.example.pizzastoreadmin.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pizzastoreadmin.data.localdb.PizzaDao
import com.example.pizzastoreadmin.data.localdb.entity.orders.ListOrdersDbModel
import com.example.pizzastoreadmin.data.localdb.entity.orders.OrderDbModel
import com.example.pizzastoreadmin.data.localdb.entity.products.ProductDbModel
import com.example.pizzastoreadmin.data.mappers.LocalMapper
import com.example.pizzastoreadmin.data.mappers.RemoteMapper
import com.example.pizzastoreadmin.data.remotedb.FirebaseService
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.Product

class LoadOrdersWorker(
    context: Context,
    private val workerParameters: WorkerParameters,
    private val firebaseService: FirebaseService,
    private val pizzaDao: PizzaDao,
    private val remoteMapper: RemoteMapper,
    private val localMapper: LocalMapper
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        firebaseService.getListOrdersFlow()
            .collect { listOrdersDto ->
                var productsDbModel = listOf<ProductDbModel>()
                val productsList = mutableListOf<Product>()
                pizzaDao.getProducts().collect daoCollect@{ collectedProductList ->

                    if (collectedProductList != null) {
                        productsDbModel = collectedProductList.products
                    } else {
                        return@daoCollect
                    }

                    productsDbModel.forEach { productDbModel ->
                        val currentProduct = localMapper.dbModelToProduct(productDbModel)
                        productsList.add(currentProduct)
                    }

                    val listOrders = mutableListOf<Order>()
                    listOrdersDto.forEach { orderDtoFromList ->
                        val currentOrder = remoteMapper.mapOrderDtoToEntity(
                            orderDtoFromList,
                            productsList
                        )
                        if (currentOrder != null) {
                            listOrders.add(currentOrder)
                        }
                    }

                    val listModelOrders = mutableListOf<OrderDbModel>()
                    listOrders.forEach { orderFromList ->
                        val currentOrder = localMapper.mapOrderToOrderModel(orderFromList)
                        listModelOrders.add(currentOrder)
                    }
                    pizzaDao.addOrders(ListOrdersDbModel(listModelOrders))
                }
            }

        return Result.success()
    }
}