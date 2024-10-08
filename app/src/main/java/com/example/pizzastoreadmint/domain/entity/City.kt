package com.example.pizzastoreadmint.domain.entity

import android.os.Parcelable
import com.example.pizzastoreadmint.domain.entity.Point
import kotlinx.parcelize.Parcelize


@Parcelize
data class City(
    val id: Int = -1,
    val name: String = "",
    val points: List<Point> = listOf()
) : Parcelable
