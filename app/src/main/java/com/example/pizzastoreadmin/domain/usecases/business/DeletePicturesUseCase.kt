package com.example.pizzastoreadmin.domain.usecases.business

import android.net.Uri
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import javax.inject.Inject

class DeletePicturesUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun deletePictures(imageUri: List<Uri>) {
        repository.deletePicturesUseCase(imageUri)
    }
}