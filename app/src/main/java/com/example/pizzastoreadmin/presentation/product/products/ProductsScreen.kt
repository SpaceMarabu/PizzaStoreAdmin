package com.example.pizzastoreadmin.presentation.product.products

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastore.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.product.products.states.CurrentStates
import com.example.pizzastoreadmin.presentation.product.products.states.ProductsScreenState
import com.example.pizzastoreadmin.presentation.product.products.states.WarningState

@Composable
fun ProductsScreen(
    paddingValues: PaddingValues,
    onAddOrCityClicked: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: ProductsScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.state.collectAsState()


    when (screenState.value) {

        is ProductsScreenState.Initial -> {}

        is ProductsScreenState.Content -> {
            val currentScreenState = screenState.value as ProductsScreenState.Content
            ListProductsScreen(
                products = currentScreenState.products,
                viewModel = viewModel,
                paddingValues = paddingValues
            ) {
                onAddOrCityClicked()
            }
        }

        ProductsScreenState.Loading -> {
            CircularLoading()
        }
    }
}

//<editor-fold desc="ShowToast">
@Composable
fun ShowToast(text: String) {
    Toast.makeText(
        LocalContext.current,
        text,
        Toast.LENGTH_SHORT
    ).show()
}
//</editor-fold>

//<editor-fold desc="Экран со списком городов">
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListProductsScreen(
    products: List<Product>,
    viewModel: ProductsScreenViewModel,
    paddingValues: PaddingValues,
    onAddOrProductClicked: () -> Unit
) {

    val warningState = viewModel.warningState.collectAsState()

    val stateHolder = remember {
        mutableStateOf(
            CurrentStates(
                productsToDelete = mutableSetOf(),
                isProductsToDeleteEmpty = true,
                isButtonClicked = false,
                isItemClicked = false,
                currentProduct = null
            )
        )

    }
    val currentStateValue = stateHolder.value

    if (currentStateValue.isButtonClicked) {
        if (!currentStateValue.isProductsToDeleteEmpty) {
            SideEffect {
                viewModel.deleteProduct(currentStateValue.productsToDelete.toList())
            }
        } else {
            SideEffect {
                viewModel.setCurrentProduct()
            }
            onAddOrProductClicked()
        }
        stateHolder.value = currentStateValue.copy(isButtonClicked = false)
    }
    if (currentStateValue.isItemClicked) {
        SideEffect {
            viewModel.setCurrentProduct(currentStateValue.currentProduct)
        }
        onAddOrProductClicked()
        stateHolder.value = currentStateValue.copy(isItemClicked = false)
    }

    when (val currentWarningStateValue = warningState.value) {
        is WarningState.DeleteComplete -> {
            ShowToast(text = currentWarningStateValue.description)
            viewModel.warningCollected()
            stateHolder.value = currentStateValue.copy(
                productsToDelete = mutableSetOf(),
                isProductsToDeleteEmpty = true
            )
        }

        is WarningState.DeleteIncomplete -> {
            ShowToast(text = currentWarningStateValue.description)
            viewModel.warningCollected()
        }

        WarningState.Nothing -> {}
    }

    Scaffold(
        modifier = Modifier
            .padding(bottom = paddingValues.calculateBottomPadding()),
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .width(100.dp)
                    .border(
                        border = BorderStroke(1.dp, Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    stateHolder.value = currentStateValue.copy(isButtonClicked = true)
                }) {
                Text(
                    text = if (currentStateValue.isProductsToDeleteEmpty) "ADD" else "DELETE",
                    fontSize = 24.sp
                )
            }
        }
    ) {
        LazyColumn {
            items(items = products, key = { it.id }) { product ->
                ProductRow(
                    product = product,
                    onClick = {
                        stateHolder.value = currentStateValue
                            .copy(
                                isItemClicked = true,
                                currentProduct = product
                            )
                    }
                ) { isBoxChecked ->
                    if (isBoxChecked) {
                        currentStateValue.productsToDelete.add(product)
                    } else {
                        currentStateValue.productsToDelete.removeIf { it.id == product.id }
                    }
                    stateHolder.value = currentStateValue
                        .copy(
                            productsToDelete = currentStateValue.productsToDelete,
                            isProductsToDeleteEmpty = currentStateValue.productsToDelete.size == 0
                        )
                }
                DividerList()
            }
        }
    }

}
//</editor-fold>


//<editor-fold desc="Строка с чекбоксом">
@Composable
fun ProductRow(
    product: Product,
    onClick: () -> Unit,
    onCheckboxChanged: (Boolean) -> Unit
) {
    var isCheckedProduct by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isCheckedProduct,
            onCheckedChange = {
                isCheckedProduct = it
                onCheckboxChanged(isCheckedProduct)
            }
        )
        Text(
            text = product.name,
            fontSize = 24.sp
        )
    }
}
//</editor-fold>


//<editor-fold desc="Разделитель">
@Composable
fun DividerList() {
    Divider(
        modifier = Modifier
            .padding(start = 8.dp, top = 8.dp),
        color = Color.Gray,
        thickness = 1.dp
    )
}
//</editor-fold>
