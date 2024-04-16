package com.example.pizzastoreadmin.presentation.product.oneproduct.states

import com.example.pizzastoreadmin.domain.entity.Product

data class ProductView(
    val product: Product = Product(),
    val isNameValid: Boolean = true,
    val isPriceValid: Boolean = true,
    val isPhotoIsNotEmpty: Boolean = true
)
