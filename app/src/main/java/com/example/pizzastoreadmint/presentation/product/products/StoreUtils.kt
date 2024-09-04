package com.example.pizzastoreadmint.presentation.product.products

import com.example.pizzastoreadmint.domain.entity.Product
import com.example.pizzastoreadmint.domain.entity.ProductType
import com.example.pizzastoreadmint.presentation.product.products.ProductStore.State

fun getAllProductTypes() = listOf(
    ProductType.PIZZA,
    ProductType.ROLL,
    ProductType.STARTER,
    ProductType.DESSERT,
    ProductType.DRINK
)

fun sortListProducts(products: List<Product>): List<Product> {
    val types = getAllProductTypes()
    val resultList = mutableListOf<Product>()
    types.forEach { currentType ->
        val currentTypeProducts =
            products.filter { currentProduct ->
                currentProduct.type == currentType
            }
        resultList.addAll(currentTypeProducts)
    }
    return resultList
}

fun updateTypeIndexes(
    indexes: Map<ProductType, Int>,
    products: List<Product>
): Map<ProductType, Int> {
    val newIndexesMap = mutableMapOf<ProductType, Int>()
    products.forEachIndexed indexedLoop@{ index, product ->
        val currentType = product.type
        val foundTypeFromMapByCurrentType = indexes[currentType] ?: -1
        if (foundTypeFromMapByCurrentType != -1 && foundTypeFromMapByCurrentType > index) {
            newIndexesMap[currentType] = index
        }
    }
    return newIndexesMap
}

fun getInitialProductIndexMap(): MutableMap<ProductType, Int> {
    val resultMap = mutableMapOf<ProductType, Int>()
    getAllProductTypes().forEach {
        resultMap[it] = 999_999_999
    }
    return resultMap
}

fun List<Product>.getButtonState(): State.ButtonState {
    return if (this.isEmpty()) {
        State.ButtonState.Add
    } else {
        State.ButtonState.Delete
    }
}