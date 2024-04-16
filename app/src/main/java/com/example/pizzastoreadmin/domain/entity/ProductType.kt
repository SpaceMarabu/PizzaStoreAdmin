package com.example.pizzastoreadmin.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ProductType(
    val type: String
) : Parcelable {

    object PIZZA : ProductType(TYPE_PIZZA)
    object ROLL : ProductType(TYPE_ROLL)
    object STARTER : ProductType(TYPE_STARTER)
    object DESSERT : ProductType(TYPE_DESSERT)
    object DRINK : ProductType(TYPE_DRINK)

    companion object {
        private const val TYPE_PIZZA = "pizza"
        private const val TYPE_ROLL = "roll"
        private const val TYPE_STARTER = "starter"
        private const val TYPE_DESSERT = "dessert"
        private const val TYPE_DRINK = "drink"
    }
}