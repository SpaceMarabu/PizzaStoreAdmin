package com.example.pizzastoreadmin.domain.entity

import android.os.Parcelable
import com.example.pizzastore.domain.entity.DeliveryType
import com.example.pizzastore.domain.entity.Point
import kotlinx.parcelize.Parcelize


@Parcelize
data class Product(
    val id: Int = -1,
    val type: ProductType = ProductType.PIZZA,
    val name: String = "",
    val price: Int = 0,
    val photo: String? = null,
    val description: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit," +
            " sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
) : Parcelable
