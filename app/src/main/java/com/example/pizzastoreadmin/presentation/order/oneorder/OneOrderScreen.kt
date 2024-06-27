package com.example.pizzastoreadmin.presentation.order.oneorder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastore.R
import com.example.pizzastore.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.funs.DividerList
import com.example.pizzastoreadmin.presentation.funs.getScreenWidthDp

@Composable
fun OrdersScreen(
    paddingValues: PaddingValues,
    onOrderClicked: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: OneOrderScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.screenState.collectAsState()

    when (val currentScreenState = screenState.value) {

        is OneOrderScreenState.Content -> {
            OrdersScreenContent(
                paddingValues = paddingValues,
                orders = currentScreenState.orders,
                viewModel = viewModel
            ) {
                onOrderClicked()
            }
        }

        OneOrderScreenState.Initial -> {}

        OneOrderScreenState.Loading -> {
            CircularLoading()
        }

    }
}

@Composable
fun OrdersScreenContent(
    paddingValues: PaddingValues,
    orders: List<Order>,
    viewModel: OneOrderScreenViewModel,
    onOrderClicked: () -> Unit
) {

}

