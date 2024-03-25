package com.example.pizzastoreadmin.domain.entity

import android.os.Parcelable
import com.example.pizzastore.domain.entity.DeliveryType
import com.example.pizzastore.domain.entity.Point
import kotlinx.parcelize.Parcelize


@Parcelize
data class City(
    val id: Int = -1,
    val name: String = "Москва",
    val deliveryType: DeliveryType = DeliveryType.TAKE_OUT,
    val points: List<Point> = listOf()
) : Parcelable
