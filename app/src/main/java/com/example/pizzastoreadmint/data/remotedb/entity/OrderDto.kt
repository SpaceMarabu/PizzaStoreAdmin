package com.example.pizzastoreadmint.data.remotedb.entity

data class OrderDto(
    val id: Int,
    val status: String,
    val bucket: BucketDto
)
