package com.example.pizzastoreadmint.presentation.product.products

import com.example.pizzastoreadmint.domain.entity.ProductType

sealed interface LabelEvent {

    data object AddOrEditProduct: LabelEvent
    data object DeleteComplete: LabelEvent
    data object DeleteFailed: LabelEvent
    data class TypeClicked(val type: ProductType): LabelEvent
}