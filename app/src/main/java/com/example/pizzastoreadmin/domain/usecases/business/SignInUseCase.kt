package com.example.pizzastoreadmin.domain.usecases.business

import com.example.pizzastoreadmin.domain.entity.User
import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun getUserUseCase(): StateFlow<User?> {
        return repository.getUserUseCase()
    }

    suspend fun signInWithEmail(email: String, password: String) {
        repository.signInWithEmail(email, password)
    }

    suspend fun signInWithSavedAccounts() {
        repository.signInWithSavedAccounts()
    }

    val sigInEvents = repository.signInEvents
}