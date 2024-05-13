package com.example.pizzastoreadmin.domain.usecases.business

import com.example.pizzastoreadmin.domain.repository.PizzaStoreRepository
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.Product
import javax.inject.Inject

class DeleteProductUseCase  @Inject constructor(
    private val repository: PizzaStoreRepository
) {

    fun deleteProduct(products: List<Product>) {
        repository.deleteProductsUseCase(products)
    }
}