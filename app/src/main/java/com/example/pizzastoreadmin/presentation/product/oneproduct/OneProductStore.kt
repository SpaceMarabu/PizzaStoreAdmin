package com.example.pizzastoreadmin.presentation.product.oneproduct

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.entity.ProductType
import com.example.pizzastoreadmin.domain.usecases.business.AddOrEditProductUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetCurrentProductUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.domain.usecases.service.SetCurrentProductUseCase
import com.example.pizzastoreadmin.presentation.funs.dropdown.DropDownMenuStates
import com.example.pizzastoreadmin.presentation.product.oneproduct.OneProductStore.Intent
import com.example.pizzastoreadmin.presentation.product.oneproduct.OneProductStore.Label
import com.example.pizzastoreadmin.presentation.product.oneproduct.OneProductStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface OneProductStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data object PictureClick : Intent

        data class TypeClick(val type: ProductType) : Intent

        data class PriceChange(val price: String) : Intent

        data class DescriptionChange(val descr: String) : Intent

        data class ProductNameChange(val name: String) : Intent

        data object DoneClick : Intent

        data object DropDownClick : Intent
    }

    data class State(
        val contentState: ContentState,
        val isDropDownExpanded: Boolean = false,
        val listProductTypes: List<ProductType>
    ) {

        sealed interface ContentState {

            data object Initial : ContentState

            data object Loading : ContentState

            data object Error : ContentState

            data class Content(val product: Product) : ContentState
        }
    }

    sealed interface Label {

        data object ErrorRepositoryResponse : Label

        data object PictureClick : Label

        data object ExitScreen : Label
    }
}

class OneProductStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val addOrEditProductUseCase: AddOrEditProductUseCase,
    private val getCurrentProductUseCase: GetCurrentProductUseCase,
    private val setCurrentProductUseCase: SetCurrentProductUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) {

    fun create(): OneProductStore =
        object : OneProductStore, Store<Intent, State, Label> by storeFactory.create(
            name = "OneProductStore",
            initialState = State(
                contentState = State.ContentState.Initial,
                listProductTypes = getAllProductTypes()
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data object LoadingProducts : Action

        data object ErrorProductLoading : Action

        data class ProductLoaded(val product: Product) : Action

        data object ErrorRepositoryResponse : Action

        data object SuccessRepositoryResponse : Action
    }

    private sealed interface Msg {

        data class TypeClick(val type: ProductType) : Msg

        data class PriceChange(val price: String) : Msg

        data class DescriptionChange(val descr: String) : Msg

        data class ProductNameChange(val name: String) : Msg

        data object LoadingProducts : Msg

        data object ErrorProductsLoading : Msg

        data class ProductLoaded(val product: Product) : Msg

        data object DropDownClick : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {

            try {
                scope.launch {
                    dispatch(Action.LoadingProducts)
                    getCurrentProductUseCase.getProduct()
                        .collect { product ->
                            dispatch(Action.ProductLoaded(product))
                        }
                }
            } catch (e: Exception) {
                dispatch(Action.ErrorProductLoading)
            }

            scope.launch {
                getDbResponseUseCase.getDbResponseFlow().collect {
                    when (it) {
                        DBResponse.Complete -> {
                            dispatch(Action.SuccessRepositoryResponse)
                        }

                        is DBResponse.Error -> {
                            dispatch(Action.ErrorRepositoryResponse)
                        }

                        DBResponse.Processing -> {}
                    }
                }
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {

            when (intent) {
                is Intent.DescriptionChange -> {
                    dispatch(Msg.DescriptionChange(intent.descr))
                }

                Intent.DoneClick -> {
                    val currentContentState = getState().contentState
                    if (currentContentState is State.ContentState.Content) {
                        addOrEditProductUseCase.addOrEditProduct(currentContentState.product)
                    }
                }

                Intent.PictureClick -> {
                    publish(Label.PictureClick)
                }

                is Intent.PriceChange -> {
                    dispatch(Msg.PriceChange(intent.price))
                }

                is Intent.TypeClick -> {
                    dispatch(Msg.TypeClick(intent.type))
                }

                is Intent.ProductNameChange -> {
                    dispatch(Msg.ProductNameChange(intent.name))
                }

                Intent.DropDownClick -> {
                    dispatch(Msg.DropDownClick)
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {

            when (action) {
                Action.ErrorProductLoading -> {
                    dispatch(Msg.ErrorProductsLoading)
                }

                Action.ErrorRepositoryResponse -> {
                    publish(Label.ErrorRepositoryResponse)
                }

                Action.LoadingProducts -> {
                    dispatch(Msg.LoadingProducts)
                }

                is Action.ProductLoaded -> {
                    dispatch(Msg.ProductLoaded(action.product))
                }

                Action.SuccessRepositoryResponse -> {
                    publish(Label.ExitScreen)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.DescriptionChange -> {
                    this.changeContentState(descriptionProduct = msg.descr)
                }

                Msg.ErrorProductsLoading -> {
                    this.copy(contentState = State.ContentState.Error)
                }

                Msg.LoadingProducts -> {
                    this.copy(contentState = State.ContentState.Loading)
                }

                is Msg.PriceChange -> {
                    this.changeContentState(priceProduct = msg.price)
                }

                is Msg.ProductLoaded -> {
                    this.copy(contentState = State.ContentState.Content(msg.product))
                }

                is Msg.TypeClick -> {
                    this.changeContentState(productType = msg.type)
                }

                is Msg.ProductNameChange -> {
                    this.changeContentState(productName = msg.name)
                }

                Msg.DropDownClick -> {
                    this.copy(
                        isDropDownExpanded = !this.isDropDownExpanded
                    )
                }
            }
    }

    companion object {

        private fun State.changeContentState(
            productName: String? = null,
            descriptionProduct: String? = null,
            priceProduct: String? = null,
            productType: ProductType? = null
        ): State {
            return if (this.contentState is State.ContentState.Content) {

                val newProduct = if (descriptionProduct != null) {
                    this.contentState.product.copy(description = descriptionProduct)
                } else if (productName != null) {
                    this.contentState.product.copy(name = productName)
                } else if (priceProduct != null) {
                    val price = priceProduct.toIntOrNull() ?: 0
                    this.contentState.product.copy(price = price)
                } else {
                    this.contentState.product.copy(
                        type = productType ?: throw NullPointerException(
                            "changeContentState был вызван с полностью пустыми значениями"
                        )
                    )
                }

                this.copy(
                    contentState = this.contentState.copy(
                        product = newProduct
                    )
                )

            } else {
                this
            }
        }
    }
}
