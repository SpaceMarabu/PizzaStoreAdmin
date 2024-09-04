package com.example.pizzastoreadmint.domain.usecases.service

import com.example.pizzastoreadmint.data.repository.states.DBResponse
import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
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