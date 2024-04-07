package com.example.pizzastoreadmin.domain.usecases.service

import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PutImageToStorageUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun putImage(imageByte: ByteArray, name: String) {
        return repository.putImageToStorage(name, imageByte)
    }
}