package com.example.pizzastoreadmint.domain.usecases.business

import android.net.Uri
import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListPicturesUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    suspend fun getListPictures(): Flow<List<Uri>> {
        return repository.getListPicturesUseCase()
    }
}