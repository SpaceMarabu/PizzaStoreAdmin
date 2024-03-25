package com.example.pizzastore.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Point(
    val id: Int = -1,
    val address: String = "ул. Воли",
    val coords: String = "1, 1"
): Parcelable