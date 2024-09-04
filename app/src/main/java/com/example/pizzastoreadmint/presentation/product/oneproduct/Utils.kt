package com.example.pizzastoreadmint.presentation.product.oneproduct

import com.example.pizzastoreadmint.domain.entity.ProductType

fun getAllProductTypes() = listOf(
    ProductType.PIZZA,
    ProductType.ROLL,
    ProductType.STARTER,
    ProductType.DESSERT,
    ProductType.DRINK
)