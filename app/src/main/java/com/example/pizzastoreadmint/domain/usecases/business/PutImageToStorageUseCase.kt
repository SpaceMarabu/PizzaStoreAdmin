package com.example.pizzastoreadmint.domain.usecases.business

import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import javax.inject.Inject

class PutImageToStorageUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun putImage(name: String, type: String, imageByte: ByteArray) {
        return repository.putImageToStorageUseCase(name, type, imageByte)
    }
}