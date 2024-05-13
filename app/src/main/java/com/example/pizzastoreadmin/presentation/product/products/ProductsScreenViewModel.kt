package com.example.pizzastoreadmin.presentation.product.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.usecases.business.DeleteProductUseCase
import com.example.pizzastoreadmin.domain.usecases.business.GetAllProductsUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.domain.usecases.service.SetCurrentProductUseCase
import com.example.pizzastoreadmin.presentation.product.products.states.ProductsScreenState
import com.example.pizzastoreadmin.presentation.product.products.states.WarningState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProductsScreenViewModel @Inject constructor(
    private val getProductsUseCase: GetAllProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val setCurrentProductUseCase: SetCurrentProductUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ProductsScreenState>(ProductsScreenState.Initial)
    val state = _state.asStateFlow()

    private val _warningState = MutableStateFlow<WarningState>(WarningState.Nothing)
    val warningState = _warningState.asStateFlow()



    init {
        loadCities()
        subscribeDbResponse()
    }

    //<editor-fold desc="subscribeDbResponse">
    private fun subscribeDbResponse() {
        viewModelScope.launch {
            getDbResponseUseCase.getDbResponseFlow().collect {
                when (it) {
                    DBResponse.Complete -> {
                        _warningState.emit(WarningState.DeleteComplete())
                    }
                    is DBResponse.Error -> {
                        _warningState.emit(WarningState.DeleteIncomplete())
                    }
                    DBResponse.Processing -> {
                        _warningState.emit(WarningState.Nothing)
                    }
                }
            }
        }
    }
    //</editor-fold>

    private fun loadCities() {
        _state.value = ProductsScreenState.Loading
        viewModelScope.launch {
            getProductsUseCase
                .getProductsFlow()
                .filter { it.isNotEmpty() }
                .collect {
                    _state.value = ProductsScreenState.Content(it)
                }
        }
    }

    fun setCurrentProduct(product: Product? = null) {
        setCurrentProductUseCase.setProduct(product)
    }

    fun deleteProduct(products: List<Product>) {
        deleteProductUseCase.deleteProduct(products)
    }

    fun warningCollected() {
        viewModelScope.launch {
            _warningState.emit(WarningState.Nothing)
        }
    }

    fun changeScreenState(state: ProductsScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }
}