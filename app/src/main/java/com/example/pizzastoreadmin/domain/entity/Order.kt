package com.example.pizzastoreadmin.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val id: Int = DEFAULT_ID,
    val status: OrderStatus = OrderStatus.NEW,
    val bucket: Bucket = Bucket()
) : Parcelable {
    companion object {
        const val DEFAULT_ID = -1
        const val ERROR_ID = -2
    }
}
