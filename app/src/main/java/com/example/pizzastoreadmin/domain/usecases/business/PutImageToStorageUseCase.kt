package com.example.pizzastoreadmin.domain.usecases.business

import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PutImageToStorageUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun putImage(name: String, type: String, imageByte: ByteArray) {
        return repository.putImageToStorage(name, type, imageByte)
    }
}