package com.example.pizzastoreadmint.presentation.product.oneproduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.pizzastoreadmint.domain.entity.ProductType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OneProductScreenUpdater @Inject constructor(
    private val storeFactory: OneProductStoreFactory
) : ViewModel() {

    private val store = storeFactory.create()

    private val _labelEvents = MutableSharedFlow<LabelEvent>()
    val labelEvents = _labelEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            store.labels.collect {
                when (it) {
                    OneProductStore.Label.ErrorRepositoryResponse -> {
                        _labelEvents.emit(LabelEvent.ErrorRepositoryResponse)
                    }

                    OneProductStore.Label.ExitScreen -> {
                        _labelEvents.emit(LabelEvent.ExitScreen)
                    }

                    OneProductStore.Label.PictureClick -> {
                        _labelEvents.emit(LabelEvent.PictureClick)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val model = store.stateFlow

    fun clickPicture() = store.accept(OneProductStore.Intent.PictureClick)

    fun clickType(type: ProductType) = store.accept(OneProductStore.Intent.TypeClick(type))

    fun clickDropDown() = store.accept(OneProductStore.Intent.DropDownClick)

    fun changeProductName(name: String) =
        store.accept(OneProductStore.Intent.ProductNameChange(name))

    fun changeProductPrice(price: String) = store.accept(OneProductStore.Intent.PriceChange(price))

    fun changeDescription(desc: String) =
        store.accept(OneProductStore.Intent.DescriptionChange(desc))

    fun doneClick() = store.accept(OneProductStore.Intent.DoneClick)

    fun screenClick() = store.accept(OneProductStore.Intent.ScreenClick)
}