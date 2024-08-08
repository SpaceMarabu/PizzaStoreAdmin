package com.example.pizzastoreadmin.presentation.product.products

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.entity.ProductType
import com.example.pizzastoreadmin.domain.usecases.business.DeleteProductUseCase
import com.example.pizzastoreadmin.domain.usecases.business.GetAllProductsUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.domain.usecases.service.SetCurrentProductUseCase
import com.example.pizzastoreadmin.presentation.product.products.ProductStore.Intent
import com.example.pizzastoreadmin.presentation.product.products.ProductStore.Label
import com.example.pizzastoreadmin.presentation.product.products.ProductStore.State
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ProductStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class SelectProduct(val product: Product) : Intent

        data class UnselectProduct(val product: Product) : Intent

        data class ClickProduct(val product: Product) : Intent

        data object ClickButton : Intent

        data class ClickType(val type: ProductType) : Intent
    }

    data class State(
        val contentState: ContentState,
        val buttonState: ButtonState = ButtonState.Add,
        val selectedProducts: List<Product> = listOf(),
        val currentSelectedProductType: ProductType = ProductType.PIZZA,
        val typeIndexes: MutableMap<ProductType, Int>,
        val productTypes: List<ProductType>
    ) {

        sealed interface ButtonState {

            data object Add : ButtonState

            data object Delete : ButtonState
        }

        sealed interface ContentState {

            data object Initial : ContentState

            data object Loading : ContentState

            data object Error : ContentState

            data class Content(val products: List<Product>) : ContentState
        }
    }

    sealed interface Label {

        data object AddProduct : Label

        data class EditProduct(val product: Product) : Label

        data class ClickType(val type: ProductType) : Label

        data object DeleteComplete : Label

        data object DeleteFailed : Label
    }
}

class ProductStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getProductsUseCase: GetAllProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val setCurrentProductUseCase: SetCurrentProductUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) {

    fun create(): ProductStore =
        object : ProductStore, Store<Intent, State, Label> by storeFactory.create(
            name = "ProductStore",
            initialState = State(
                contentState = State.ContentState.Initial,
                typeIndexes = getInitialProductIndexMap(),
                productTypes = getAllProductTypes()
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data object LoadingProducts : Action

        data object ErrorProductsLoading : Action

        data class ProductsLoaded(val products: List<Product>) : Action

        data object DeleteComplete : Action

        data object DeleteFailed : Action
    }

    private sealed interface Msg {

        data object LoadingProducts : Msg

        data object ErrorProductsLoading : Msg

        data class ProductsLoaded(val products: List<Product>) : Msg

        data class SelectProduct(val product: Product) : Msg

        data class UnselectProduct(val product: Product) : Msg

        data object DeleteProducts : Msg

        data class ClickType(val type: ProductType) : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {

            scope.launch {
                getDbResponseUseCase.getDbResponseFlow().collect {
                    when (it) {
                        DBResponse.Complete -> {
                            dispatch(Action.DeleteComplete)
                        }

                        is DBResponse.Error -> {
                            dispatch(Action.DeleteFailed)
                        }

                        DBResponse.Processing -> {}
                    }
                }
            }

            try {
                scope.launch {
                    dispatch(Action.LoadingProducts)
                    getProductsUseCase
                        .getProductsFlow()
                        .filter { it.isNotEmpty() }
                        .collect {
                            dispatch(Action.ProductsLoaded(it))
                        }
                }
            } catch (e: Exception) {
                dispatch(Action.ErrorProductsLoading)
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {

            when (intent) {
                is Intent.SelectProduct -> {
                    dispatch(Msg.SelectProduct(intent.product))
                }

                is Intent.ClickProduct -> {
                    val currentProduct = intent.product
                    setCurrentProductUseCase.setProduct(currentProduct)
                    publish(Label.EditProduct(currentProduct))
                }

                is Intent.UnselectProduct -> {
                    dispatch(Msg.UnselectProduct(intent.product))
                }

                Intent.ClickButton -> {
                    val currentButtonState = getState().buttonState
                    when (currentButtonState) {
                        State.ButtonState.Add -> {
                            setCurrentProductUseCase.setProduct(null)
                            publish(Label.AddProduct)
                        }

                        State.ButtonState.Delete -> {
                            val currentSelectedProducts = getState().selectedProducts
                            deleteProductUseCase.deleteProduct(currentSelectedProducts)
                            dispatch(Msg.DeleteProducts)
                        }
                    }
                }

                is Intent.ClickType -> {
                    publish(Label.ClickType(intent.type))
                    dispatch(Msg.ClickType(intent.type))
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {

            when (action) {
                is Action.ProductsLoaded -> {
                    dispatch(Msg.ProductsLoaded(action.products))
                }

                Action.DeleteComplete -> {
                    publish(Label.DeleteComplete)
                }

                Action.DeleteFailed -> {
                    publish(Label.DeleteFailed)
                }

                Action.ErrorProductsLoading -> {
                    dispatch(Msg.ErrorProductsLoading)
                }

                Action.LoadingProducts -> {
                    dispatch(Msg.LoadingProducts)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {

                is Msg.ProductsLoaded -> {
                    this.copy(
                        contentState = State.ContentState.Content(
                            products = sortListProducts(msg.products)
                        )
                    )
                }

                Msg.ErrorProductsLoading -> {
                    this.copy(
                        contentState = State.ContentState.Error
                    )
                }

                Msg.LoadingProducts -> {
                    this.copy(
                        contentState = State.ContentState.Loading
                    )
                }

                is Msg.SelectProduct -> {

                    val currentSelectedProducts = this.selectedProducts.toMutableList()
                    currentSelectedProducts.add(msg.product)
                    this.copy(
                        selectedProducts = currentSelectedProducts.toList(),
                        buttonState = currentSelectedProducts.getButtonState()
                    )
                }

                is Msg.UnselectProduct -> {
                    val currentSelectedProducts = this.selectedProducts.toMutableList()
                    currentSelectedProducts.remove(msg.product)
                    this.copy(
                        selectedProducts = currentSelectedProducts.toList(),
                        buttonState = currentSelectedProducts.getButtonState()
                    )
                }

                Msg.DeleteProducts -> {
                    val selectedProducts = listOf<Product>()
                    this.copy(
                        selectedProducts = selectedProducts,
                        buttonState = selectedProducts.getButtonState()
                    )
                }

                is Msg.ClickType -> {
                    this.copy(
                        currentSelectedProductType = msg.type
                    )
                }
            }
    }

}
