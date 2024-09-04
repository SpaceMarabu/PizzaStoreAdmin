package com.example.pizzastoreadmint.presentation.product.oneproduct.states

import com.example.pizzastoreadmint.domain.entity.Product

data class ProductView(
    val product: Product = Product(),
    val isNameValid: Boolean = true,
    val isPriceValid: Boolean = true,
    val isPhotoIsNotEmpty: Boolean = true
)
