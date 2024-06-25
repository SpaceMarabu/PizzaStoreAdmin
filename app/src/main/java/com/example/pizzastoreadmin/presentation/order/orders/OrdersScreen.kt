package com.example.pizzastoreadmin.presentation.order.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastore.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.presentation.funs.CircularLoading

@Composable
fun OrdersScreen(
    paddingValues: PaddingValues,
    onAddOrCityClicked: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: OrdersScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.screenState.collectAsState()

    when (val currentScreenState = screenState.value) {

        is OrderScreenState.Content -> {
            OrdersScreenContent(
                paddingValues = paddingValues,
                orders = currentScreenState.orders,
                viewModel = viewModel
            )
        }

        OrderScreenState.Initial -> {}

        OrderScreenState.Loading -> {
            CircularLoading()
        }

    }
}

@Composable
fun OrdersScreenContent(
    paddingValues: PaddingValues,
    orders: List<Order>,
    viewModel: OrdersScreenViewModel
) {

    Column {
        Row (
            horizontalArrangement = Arrangement.End
        ){
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "Фильтр")
        }
    }
}

@Composable
fun FilterMenu(filteredList: List<OrderStatus>)

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
