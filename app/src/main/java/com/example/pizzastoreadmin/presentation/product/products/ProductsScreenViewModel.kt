package com.example.pizzastoreadmin.presentation.product.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.entity.ProductType
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

    private val _typesMap = MutableStateFlow(getInitialProductIndexMap())
    val typesMap = _typesMap.asStateFlow()

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

    //<editor-fold desc="loadCities">
    private fun loadCities() {
        _state.value = ProductsScreenState.Loading
        viewModelScope.launch {
            getProductsUseCase
                .getProductsFlow()
                .filter { it.isNotEmpty() }
                .collect {
                    val sortedListProduct = sortListProducts(it)
                    _state.value = ProductsScreenState.Content(sortedListProduct)
                }
        }
    }
    //</editor-fold>

    //<editor-fold desc="getAllProductTypes">
    fun getAllProductTypes() = listOf(
        ProductType.PIZZA,
        ProductType.ROLL,
        ProductType.STARTER,
        ProductType.DESSERT,
        ProductType.DRINK
    )
    //</editor-fold>

    //<editor-fold desc="getInitialProductIndexMap">
    private fun getInitialProductIndexMap(): MutableMap<ProductType, Int> {
        val resultMap = mutableMapOf<ProductType, Int>()
        getAllProductTypes().forEach {
            resultMap[it] = 999_999_999
        }
        return resultMap
    }
    //</editor-fold>

    //<editor-fold desc="sortListProducts">
    private fun sortListProducts(products: List<Product>): List<Product> {
        val types = getAllProductTypes()
        val resultList = mutableListOf<Product>()
        types.forEach { currentType ->
            val currentTypeProducts =
                products.filter { currentProduct ->
                    currentProduct.type == currentType
                }
            resultList.addAll(currentTypeProducts)
        }
        resultList.forEachIndexed indexedLoop@{ index, product ->
            val currentType = product.type
            val foundTypeFromMapByCurrentType = _typesMap.value[currentType] ?: -1
            if (foundTypeFromMapByCurrentType != -1 && foundTypeFromMapByCurrentType > index) {
                _typesMap.value[currentType] = index
            }
        }
        return resultList
    }
    //</editor-fold>

    //<editor-fold desc="setCurrentProduct">
    fun setCurrentProduct(product: Product? = null) {
        setCurrentProductUseCase.setProduct(product)
    }
    //</editor-fold>

    //<editor-fold desc="deleteProduct">
    fun deleteProduct(products: List<Product>) {
        deleteProductUseCase.deleteProduct(products)
    }
    //</editor-fold>

    //<editor-fold desc="warningCollected">
    fun warningCollected() {
        viewModelScope.launch {
            _warningState.emit(WarningState.Nothing)
        }
    }
    //</editor-fold>
}