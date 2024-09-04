package com.example.pizzastoreadmint.presentation.city.onecity.states

import android.os.Parcelable
import com.example.pizzastoreadmint.domain.entity.Point
import kotlinx.parcelize.Parcelize

@Parcelize
data class PointViewState(
    val point: Point,
    val isGeopointValid: Boolean = true,
    val isAddressValid: Boolean = true
) : Parcelable
