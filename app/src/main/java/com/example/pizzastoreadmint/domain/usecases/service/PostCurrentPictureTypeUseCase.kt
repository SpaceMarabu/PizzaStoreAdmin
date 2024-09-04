package com.example.pizzastoreadmint.domain.usecases.service

import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
import com.example.pizzastoreadmint.domain.entity.PictureType
import javax.inject.Inject

class PostCurrentPictureTypeUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    suspend fun postType(type: PictureType) {
        repository.postPicturesType(type)
    }
}