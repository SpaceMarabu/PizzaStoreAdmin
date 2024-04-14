package com.example.pizzastoreadmin.domain.usecases.service

import android.net.Uri
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import javax.inject.Inject

class SetCurrentProductImageUriUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun setUri(imageUri: Uri?) {
        repository.setCurrentProductImageUseCase(imageUri)
    }
}