package com.example.pizzastoreadmin.presentation.product.oneproduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.entity.ProductType
import com.example.pizzastoreadmin.domain.usecases.business.AddOrEditProductUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetCurrentProductUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.presentation.product.oneproduct.states.EditTextFieldState
import com.example.pizzastoreadmin.presentation.product.oneproduct.states.EditType
import com.example.pizzastoreadmin.presentation.product.oneproduct.states.ScreenChangingState
import com.example.pizzastoreadmin.presentation.product.oneproduct.states.OneProductScreenState
import com.example.pizzastoreadmin.presentation.product.oneproduct.states.ProductView
import com.example.pizzastoreadmin.presentation.sharedstates.ShouldLeaveScreenState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OneProductScreenViewModel @Inject constructor(
    private val addOrEditProductUseCase: AddOrEditProductUseCase,
    private val getCurrentProductUseCase: GetCurrentProductUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<OneProductScreenState>(OneProductScreenState.Initial)
    val state = _state.asStateFlow()

    private val _currentProduct = MutableStateFlow(ProductView())
    val currentProduct = _currentProduct.asStateFlow()

    private val _needCallback = MutableStateFlow(false)
    val needCallback = _needCallback.asStateFlow()

    private val _shouldLeaveScreenState: MutableStateFlow<ShouldLeaveScreenState> =
        MutableStateFlow(ShouldLeaveScreenState.Processing)
    val shouldLeaveScreenState = _shouldLeaveScreenState.asStateFlow()

    private val _screenChanges = MutableSharedFlow<ScreenChangingState>(
        extraBufferCapacity = 50
    )
    private val _resultEditStateFlow = MutableStateFlow(EditTextFieldState())

    init {
        getCurrentProduct()
        changeScreenState(OneProductScreenState.Content)
        changeScreenContent()
        subscribeDbResponse()
    }

    //<editor-fold desc="getCurrentProduct">
    private fun getCurrentProduct() {
        viewModelScope.launch {
            getCurrentProductUseCase.getProduct()
                .collect { product ->
                    _currentProduct.emit(
                        ProductView(
                            product = product
                        )
                    )
                }
        }
    }
    //</editor-fold>

    //<editor-fold desc="subscribeDbResponse">
    private fun subscribeDbResponse() {
        viewModelScope.launch {
            getDbResponseUseCase.getDbResponseFlow().collect {
                when (it) {
                    DBResponse.Complete -> {
                        _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Exit)
                    }

                    is DBResponse.Error -> {
                        _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Error(it.description))
                    }

                    DBResponse.Processing -> {
                        _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Processing)
                    }
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="changeScreenContent">
    private fun changeScreenContent() {
        viewModelScope.launch {
            _screenChanges.collect {

                val currentProductView = _currentProduct.value
                var productViewToChange = currentProductView
                var productToChange = currentProductView.product

                val resultEditFlowValue = _resultEditStateFlow.value

                when (val currentChangingState = it) {
                    is ScreenChangingState.ChangeProductDescription -> {
                        productToChange = productToChange
                            .copy(description = currentChangingState.description)
                        productViewToChange = productViewToChange
                            .copy(product = productToChange)
                        _resultEditStateFlow.value = resultEditFlowValue.copy(
                            isDescriptionCollected = true
                        )
                    }

                    is ScreenChangingState.ChangeProductName -> {
                        productToChange = productToChange
                            .copy(name = currentChangingState.name)
                        productViewToChange = productViewToChange
                            .copy(
                                product = productToChange,
                                isNameValid = currentChangingState.name.isNotBlank()
                            )
                        _resultEditStateFlow.value = resultEditFlowValue.copy(
                            isNameCollected = true
                        )
                    }

                    is ScreenChangingState.ChangeProductPhoto -> {
                        productToChange = productToChange
                            .copy(photo = currentChangingState.uri)
                        productViewToChange = productViewToChange
                            .copy(
                                product = productToChange,
                                isNameValid = currentChangingState.uri.isNotBlank()
                            )
                        _resultEditStateFlow.value = resultEditFlowValue.copy(
                            isPhotoCollected = true
                        )
                    }

                    is ScreenChangingState.ChangeProductPrice -> {
                        productToChange = productToChange
                            .copy(price = currentChangingState.price)
                        productViewToChange = productViewToChange
                            .copy(
                                product = productToChange,
                                isPriceValid = currentChangingState.price >= 0
                            )
                        _resultEditStateFlow.value = resultEditFlowValue.copy(
                            isPriceCollected = true
                        )
                    }

                    is ScreenChangingState.ChangeProductType -> {
                        productToChange = productToChange
                            .copy(type = currentChangingState.type)
                        productViewToChange = productViewToChange
                            .copy(product = productToChange)
                        _resultEditStateFlow.value = resultEditFlowValue.copy(
                            isTypeCollected = true
                        )
                    }

                    ScreenChangingState.Return -> {
                        if (checkAllDataCollected()) {
                            stopCallbackScreen()
                            val currentProductValue = _currentProduct.value
                            if (checkAllDataValid()) {
                                addOrEditProductUseCase
                                    .addOrEditProduct(currentProductValue.product)
                            }
                        } else {
                            emitChangeBackToFlow(currentChangingState)
                        }
                    }
                }
                _currentProduct.emit(productViewToChange)
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="exitScreen">
    fun exitScreen() {
        viewModelScope.launch {
            needCallbackScreen()
            _screenChanges.emit(ScreenChangingState.Return)
            _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Processing)
        }
    }
    //</editor-fold>

    //<editor-fold desc="checkAllDataCollected">
    private fun checkAllDataCollected(): Boolean {
        val currentStates = _resultEditStateFlow.value
        return currentStates.isPriceCollected
                && currentStates.isDescriptionCollected
                && currentStates.isNameCollected
                && currentStates.isTypeCollected
                && currentStates.isPhotoCollected
    }
    //</editor-fold>

    //<editor-fold desc="checkAllDataValid">
    private fun checkAllDataValid(): Boolean {
        val currentViewState = _currentProduct.value
        return currentViewState.isNameValid
                && currentViewState.isPriceValid
                && currentViewState.isPhotoIsNotEmpty
    }
    //</editor-fold>

    //<editor-fold desc="emitChangeBackToFlow">
    private suspend fun emitChangeBackToFlow(value: ScreenChangingState) {
        delay(100)
        _screenChanges.emit(value)

    }
//</editor-fold>

    //<editor-fold desc="needCallbackScreen">
    private fun needCallbackScreen() {
        viewModelScope.launch {
            _resultEditStateFlow.emit(EditTextFieldState())
            _needCallback.emit(true)
        }
    }
    //</editor-fold>

    //<editor-fold desc="stopCallbackScreen">
    private fun stopCallbackScreen() {
        viewModelScope.launch {
            _needCallback.emit(false)
        }
    }
//</editor-fold>

    //<editor-fold desc="editProduct">
    fun editProduct(type: EditType, value: String) {
        viewModelScope.launch {
            val changingState = when(type) {
                EditType.NAME -> {
                    ScreenChangingState.ChangeProductName(value)
                }
                EditType.PRICE -> {
                    ScreenChangingState.ChangeProductPrice(value.toInt())
                }
                EditType.PHOTO -> {
                    ScreenChangingState.ChangeProductPhoto(value)
                }
                EditType.DESCRIPTION -> {
                    ScreenChangingState.ChangeProductDescription(value)
                }
            }
            _screenChanges.emit(changingState)
        }
    }
    //</editor-fold>

    //<editor-fold desc="getAllProductTypes">
    fun getAllProductTypes() = listOf(
        PictureType.PIZZA,
        PictureType.ROLL,
        PictureType.STARTER,
        PictureType.DESSERT,
        PictureType.DRINK,
        PictureType.STORY
    )
    //</editor-fold>

    fun editProduct(value: ProductType) {
        viewModelScope.launch {
            val changingState = ScreenChangingState.ChangeProductType(value)
            _screenChanges.emit(changingState)
        }
    }


    private fun changeScreenState(state: OneProductScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }
}