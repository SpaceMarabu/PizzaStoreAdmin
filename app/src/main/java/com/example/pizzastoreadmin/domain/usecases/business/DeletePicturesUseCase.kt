package com.example.pizzastoreadmin.domain.usecases.business

import android.net.Uri
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import javax.inject.Inject

class DeletePicturesUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun deletePictures(imageUri: List<Uri>) {
        repository.deleteImagesUseCase(imageUri)
    }
}