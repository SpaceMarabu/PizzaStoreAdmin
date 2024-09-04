package com.example.pizzastoreadmint.domain.usecases.service

import android.net.Uri
import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
import javax.inject.Inject

class SetCurrentProductImageUriUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun setUri(imageUri: Uri?) {
        repository.setCurrentProductImageUseCase(imageUri)
    }
}