package com.example.pizzastoreadmin.domain.usecases.service

import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.PictureType
import javax.inject.Inject

class PostCurrentPictureTypeUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    suspend fun postType(type: PictureType) {
        repository.postPicturesType(type)
    }
}