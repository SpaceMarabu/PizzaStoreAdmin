package com.example.pizzastoreadmin.data.localdb.entity.products

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize


@Entity
data class ProductDbModel(
    val id: Int = -1,
    val type: String = "0",
    val name: String = "",
    val price: Int = 0,
    val photo: String? = null,
    val description: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit," +
            " sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
)
