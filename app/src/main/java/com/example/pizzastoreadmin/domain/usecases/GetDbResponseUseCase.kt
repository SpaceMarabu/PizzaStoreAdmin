package com.example.pizzastoreadmin.domain.usecases

import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetDbResponseUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getDbResponseFlow(): StateFlow<DBResponse> {
        return repository.getDbResponse()
    }
}