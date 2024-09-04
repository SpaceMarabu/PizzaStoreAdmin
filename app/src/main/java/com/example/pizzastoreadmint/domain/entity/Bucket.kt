package com.example.pizzastoreadmint.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bucket(
    val order: Map<Product, Int> = mapOf()
): Parcelable
