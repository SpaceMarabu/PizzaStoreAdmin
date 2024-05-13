package com.example.pizzastoreadmin.presentation.product.products.states

import com.example.pizzastoreadmin.domain.entity.Product

sealed class ProductsScreenState() {

    object Initial : ProductsScreenState()
    object Loading : ProductsScreenState()

    data class Content(
        val products: List<Product>
    ): ProductsScreenState()

}
