package com.example.pizzastoreadmin.presentation.product.oneproduct

import com.example.pizzastoreadmin.domain.entity.ProductType

fun getAllProductTypes() = listOf(
    ProductType.PIZZA,
    ProductType.ROLL,
    ProductType.STARTER,
    ProductType.DESSERT,
    ProductType.DRINK
)