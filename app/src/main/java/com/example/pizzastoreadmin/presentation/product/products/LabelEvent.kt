package com.example.pizzastoreadmin.presentation.product.products

import com.example.pizzastoreadmin.domain.entity.ProductType

sealed interface LabelEvent {

    data object AddOrEditProduct: LabelEvent
    data object DeleteComplete: LabelEvent
    data object DeleteFailed: LabelEvent
    data class TypeClicked(val type: ProductType): LabelEvent
}