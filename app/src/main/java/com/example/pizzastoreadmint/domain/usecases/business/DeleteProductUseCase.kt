package com.example.pizzastoreadmint.domain.usecases.business

import com.example.pizzastoreadmint.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmint.domain.entity.City
import com.example.pizzastoreadmint.domain.entity.Product
import javax.inject.Inject

class DeleteProductUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun deleteProduct(products: List<Product>) {
        repository.deleteProductsUseCase(products)
    }
}