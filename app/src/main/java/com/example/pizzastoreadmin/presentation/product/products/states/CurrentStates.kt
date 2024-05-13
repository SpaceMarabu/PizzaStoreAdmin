package com.example.pizzastoreadmin.presentation.product.products.states

import com.example.pizzastoreadmin.domain.entity.Product

data class CurrentStates(
    var productsToDelete: MutableSet<Product>,
    var isProductsToDeleteEmpty: Boolean,
    var isButtonClicked: Boolean,
    var isItemClicked: Boolean,
    val currentProduct: Product?
)
