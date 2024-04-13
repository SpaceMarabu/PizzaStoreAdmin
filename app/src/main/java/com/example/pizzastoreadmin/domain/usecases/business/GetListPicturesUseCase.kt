package com.example.pizzastoreadmin.domain.usecases.business

import android.net.Uri
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListPicturesUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    suspend fun getListPictures(): Flow<List<Uri>> {
        return repository.getListPicturesUseCase()
    }
}