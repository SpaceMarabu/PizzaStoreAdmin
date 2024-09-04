package com.example.pizzastoreadmint.presentation.product.products

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastoreadmint.R
import com.example.pizzastoreadmint.di.getApplicationComponent
import com.example.pizzastoreadmint.domain.entity.Product
import com.example.pizzastoreadmint.domain.entity.ProductType
import com.example.pizzastoreadmint.presentation.funs.CircularLoading
import com.example.pizzastoreadmint.presentation.funs.DividerList
import com.example.pizzastoreadmint.presentation.funs.showToastWarn

@Composable
fun ProductsScreen(
    paddingValues: PaddingValues,
    onAddOrCityClicked: () -> Unit
) {

    val component = getApplicationComponent()
    val updater: ProductsScreenUpdater = viewModel(factory = component.getViewModelFactory())

    val model by updater.model.collectAsState()

    val currentContext = LocalContext.current
    val deletingSuccessText = stringResource(R.string.success_deleting)
    val deletingFailedText = stringResource(R.string.failed)

    val lazyListState = rememberLazyListState()

    val clickedType: MutableState<ProductType?> = remember {
        mutableStateOf(null)
    }

    LaunchedEffect(key1 = Unit) {
        updater.labelEvents.collect {
            when (it) {
                LabelEvent.DeleteComplete -> {
                    showToastWarn(currentContext, deletingSuccessText)
                }

                LabelEvent.DeleteFailed -> {
                    showToastWarn(currentContext, deletingFailedText)
                }

                LabelEvent.AddOrEditProduct -> {
                    onAddOrCityClicked()
                }

                is LabelEvent.TypeClicked -> {
                    clickedType.value = it.type
                }
            }
        }
    }

    if (clickedType.value != null) {
        LaunchedEffect(key1 = clickedType) {
            val indexType = model.typeIndexes[clickedType.value]
            lazyListState.animateScrollToItem(index = indexType ?: 0)
            clickedType.value = null
        }
    }

    val buttonText = when (model.buttonState) {
        ProductStore.State.ButtonState.Add -> stringResource(id = R.string.add_button)
        ProductStore.State.ButtonState.Delete -> stringResource(id = R.string.delete_button)
    }


    when (val currentContentState = model.contentState) {

        is ProductStore.State.ContentState.Content -> {
            ListProductsScreen(
                products = currentContentState.products,
                clickedType = model.currentSelectedProductType,
                listProductTypes = model.productTypes,
                buttonText = buttonText,
                paddingValues = paddingValues,
                lazyListState = lazyListState,
                onButtonClick = { updater.buttonClick() },
                onProductTypeClick = { updater.typeClick(it) },
                onSelectProductClick = { updater.selectProduct(it) },
                onUnselectProductClick = { updater.unselectProduct(it) },
                onProductClick = { updater.productClick(it) }
            )
        }

        ProductStore.State.ContentState.Error -> {}
        ProductStore.State.ContentState.Initial -> {}
        ProductStore.State.ContentState.Loading -> {
            CircularLoading()
        }
    }

}


//<editor-fold desc="Экран со списком продуктов">
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListProductsScreen(
    products: List<Product>,
    clickedType: ProductType,
    listProductTypes: List<ProductType>,
    buttonText: String,
    paddingValues: PaddingValues,
    lazyListState: LazyListState,
    onButtonClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    onSelectProductClick: (Product) -> Unit,
    onUnselectProductClick: (Product) -> Unit,
    onProductTypeClick: (ProductType) -> Unit
) {

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
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    onButtonClick()
                }) {
                Text(
                    text = buttonText,
                    fontSize = 24.sp
                )
            }
        }
    ) {
        Column {
            LazyRow(
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                items(items = listProductTypes) { productType ->
                    Row(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .width(150.dp)
                            .height(40.dp)
                            .background(
                                if (clickedType == productType) {
                                    Color.LightGray
                                } else {
                                    Color.LightGray.copy(alpha = 0.3f)
                                }
                            )
                            .clickable {
                                onProductTypeClick(productType)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = productType.type,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .padding(top = 8.dp)
            )
            LazyColumn(
                state = lazyListState
            ) {
                items(items = products) { product ->
                    ProductRow(
                        product = product,
                        onClick = {
                            onProductClick(product)
                        }
                    ) { isBoxChecked ->
                        if (isBoxChecked) {
                            onSelectProductClick(product)
                        } else {
                            onUnselectProductClick(product)
                        }
                    }
                    DividerList()
                }
            }
        }
    }
}
//</editor-fold>

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
            .fillMaxWidth()
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
