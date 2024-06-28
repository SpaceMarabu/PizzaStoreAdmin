package com.example.pizzastoreadmin.presentation.order.oneorder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.example.pizzastoreadmin.presentation.funs.getScreenWidthDp
import com.example.pizzastoreadmin.presentation.order.orders.OrdersScreenViewModel
import com.example.pizzastoreadmin.presentation.order.sharedfun.getStatusColor

@Composable
fun OrdersScreen(
    paddingValues: PaddingValues
) {

    val component = getApplicationComponent()
    val viewModel: OneOrderScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.screenState.collectAsState()

    when (val currentScreenState = screenState.value) {

        is OneOrderScreenState.Content -> {
            OrdersScreenContent(
                paddingValues = paddingValues,
                order = currentScreenState.order,
                viewModel = viewModel
            )
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
    order: Order,
    viewModel: OneOrderScreenViewModel
) {
    var currentOrderState by remember {
        mutableStateOf(order.status)
    }
    var statusMenuExpanded by remember {
        mutableStateOf(false)
    }

    val thirdScreenWidth = getScreenWidthDp() / 3
    val statuses = viewModel.listOrderStatuses

    Column (
        modifier = Modifier
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    statusMenuExpanded = !statusMenuExpanded
                }
        ) {
            Text(text = order.id.toString())
            Text(text = currentOrderState.name)
        }
        if (statusMenuExpanded) {
            StatusesMenu(widthMenu = thirdScreenWidth, statuses = statuses) {
                currentOrderState = it
            }
        }
    }
}

//<editor-fold desc="FilterMenu">
@Composable
fun StatusesMenu(
    widthMenu: Dp,
    statuses: List<OrderStatus>,
    onStatusClicked: (OrderStatus) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(end = 8.dp)
            .zIndex(2f),
        horizontalArrangement = Arrangement.End
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .width(widthMenu)
                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
        ) {
            LazyColumn {
                items(statuses) { orderStatus ->

                    val statusColor = getStatusColor(orderStatus)
                    Row(
                        modifier = Modifier
                            .background(statusColor)
                            .width(widthMenu)
                            .padding(
                                start = 4.dp,
                                top = 4.dp,
                                bottom = 4.dp,
                                end = 8.dp
                            )
                            .clickable {
                                onStatusClicked(orderStatus)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = orderStatus.name,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
//</editor-fold>









