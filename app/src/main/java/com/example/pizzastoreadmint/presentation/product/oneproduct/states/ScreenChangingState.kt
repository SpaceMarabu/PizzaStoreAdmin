package com.example.pizzastoreadmint.presentation.product.oneproduct.states

import com.example.pizzastoreadmint.domain.entity.ProductType

sealed class ScreenChangingState() {

    data class ChangeProductName(
        val name: String
    ) : ScreenChangingState()

    data class ChangeProductType(
        val type: ProductType
    ) : ScreenChangingState()

    data class ChangeProductPrice(
        val price: Int
    ) : ScreenChangingState()

    data class ChangeProductPhoto(
        val uri: String
    ) : ScreenChangingState()

    data class ChangeProductDescription(
        val description: String
    ) : ScreenChangingState()

    object Return : ScreenChangingState()
}
