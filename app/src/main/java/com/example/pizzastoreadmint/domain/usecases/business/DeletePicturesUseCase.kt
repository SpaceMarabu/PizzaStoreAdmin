package com.example.pizzastoreadmint.domain.usecases.business

import android.net.Uri
import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import javax.inject.Inject

class DeletePicturesUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun deletePictures(imageUri: List<Uri>) {
        repository.deleteImagesUseCase(imageUri)
    }
}