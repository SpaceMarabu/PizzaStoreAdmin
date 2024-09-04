package com.example.pizzastoreadmint.presentation.product.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.pizzastoreadmint.domain.entity.Product
import com.example.pizzastoreadmint.domain.entity.ProductType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProductsScreenUpdater @Inject constructor(
    private val storeFactory: ProductStoreFactory
) : ViewModel() {

    private val store = storeFactory.create()

    private val _labelEvents = MutableSharedFlow<LabelEvent>()
    val labelEvents = _labelEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            store.labels.collect {
                when (it) {
                    ProductStore.Label.AddProduct -> {
                        _labelEvents.emit(LabelEvent.AddOrEditProduct)
                    }

                    is ProductStore.Label.ClickType -> {
                        _labelEvents.emit(LabelEvent.TypeClicked(it.type))
                    }

                    ProductStore.Label.DeleteComplete -> {
                        _labelEvents.emit(LabelEvent.DeleteComplete)
                    }

                    ProductStore.Label.DeleteFailed -> {
                        _labelEvents.emit(LabelEvent.DeleteFailed)
                    }

                    is ProductStore.Label.EditProduct -> {
                        _labelEvents.emit(LabelEvent.AddOrEditProduct)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val model = store.stateFlow

    fun selectProduct(product: Product) = store.accept(ProductStore.Intent.SelectProduct(product))

    fun unselectProduct(product: Product) =
        store.accept(ProductStore.Intent.UnselectProduct(product))

    fun buttonClick() = store.accept(ProductStore.Intent.ClickButton)

    fun productClick(product: Product) = store.accept(ProductStore.Intent.ClickProduct(product))

    fun typeClick(type: ProductType) = store.accept(ProductStore.Intent.ClickType(type))

}