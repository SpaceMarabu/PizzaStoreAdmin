package com.example.pizzastoreadmint.domain.usecases.business

import com.example.pizzastoreadmint.domain.entity.User
import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
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

    suspend fun createUser(email: String, password: String) {
        repository.createUserWithEmail(email, password)
    }

    suspend fun signInWithSavedAccounts() {
        repository.signInWithSavedAccounts()
    }

    val signInEvents = repository.signInEvents

    suspend fun signOut() {
        return repository.signOut()
    }
}