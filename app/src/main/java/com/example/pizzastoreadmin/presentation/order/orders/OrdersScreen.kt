package com.example.pizzastoreadmin.presentation.order.orders

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
import androidx.compose.material.Divider
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
    val viewModel: OrdersScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.screenState.collectAsState()

    when (val currentScreenState = screenState.value) {

        is OrderScreenState.Content -> {
            OrdersScreenContent(
                paddingValues = paddingValues,
                orders = currentScreenState.orders,
                viewModel = viewModel
            ) {
                onOrderClicked()
            }
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
    viewModel: OrdersScreenViewModel,
    onOrderClicked: () -> Unit
) {

    var filterExpanded by remember {
        mutableStateOf(false)
    }
    val filterState by viewModel.currentFilterFlow.collectAsState()

    val thirdScreenWidth = getScreenWidthDp() / 3
    val filterButtonShape = if (filterExpanded) {
        RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
    } else {
        RoundedCornerShape(10.dp)
    }

    Column(
        modifier = Modifier
            .padding(start = 8.dp, top = 8.dp)
    ) {

        FilterButton(
            width = thirdScreenWidth,
            shape = filterButtonShape
        ) {
            filterExpanded = !filterExpanded
        }

        Box {

            if (filterExpanded) {
                FilterMenu(
                    filterState = filterState,
                    viewModel = viewModel,
                    widthMenu = thirdScreenWidth
                ) { orderStatus, value ->
                    viewModel.changeFilter(orderStatus, value)
                }
            }

            LazyOrders(
                paddingValues = paddingValues,
                orders = orders,
                viewModel = viewModel
            ) {
                viewModel.setOrder(it)
                onOrderClicked()
            }

        }
    }
}

//<editor-fold desc="LazyOrders">
@Composable
fun LazyOrders(
    paddingValues: PaddingValues,
    orders: List<Order>,
    viewModel: OrdersScreenViewModel,
    onOrderClicked: (Order) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .zIndex(1f)
            .padding(bottom = paddingValues.calculateBottomPadding()),
    ) {
        items(orders) { order ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, end = 8.dp)
                    .clickable {
                        onOrderClicked(order)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "№ ${order.id}", fontSize = 26.sp)
                Text(
                    modifier = Modifier
                        .background(getStatusColor(order.status)),
                    text = viewModel.getOrderStatusDescription(order.status),
                    fontSize = 20.sp
                )
            }
            DividerList()
        }
    }
}
//</editor-fold>

//<editor-fold desc="FilterButton">
@Composable
fun FilterButton(width: Dp, shape: Shape, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.End
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .padding(end = 8.dp)
                .width(width)
                .height(25.dp)
                .clip(shape)
                .background(Color.LightGray.copy(alpha = 0.3f)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .clickable {
                        onClick()
                    },
                text = "Статус",
                fontSize = 16.sp
            )
        }

    }
}
//</editor-fold>

//<editor-fold desc="FilterMenu">
@Composable
fun FilterMenu(
    filterState: FilterState,
    viewModel: OrdersScreenViewModel,
    widthMenu: Dp,
    onFilterClicked: (OrderStatus, Boolean) -> Unit
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
                .background(Color.White)
        ) {
            val listFilters = filterState.filterMap.keys.toList()
            LazyColumn {
                items(listFilters) { orderStatus ->
                    val isCurrentStatusFiltered = filterState.filterMap[orderStatus] ?: false
                    FilterRow(
                        orderStatusDescription = viewModel.getOrderStatusDescription(orderStatus),
                        isChecked = !isCurrentStatusFiltered,
                        widthElement = widthMenu,
                        statusColor = getStatusColor(orderStatus)
                    ) {
                        onFilterClicked(orderStatus, it)
                    }
                }
            }
        }
    }
}
//</editor-fold>

//<editor-fold desc="FilterRow">
@Composable
fun FilterRow(
    orderStatusDescription: String,
    isChecked: Boolean,
    widthElement: Dp,
    statusColor: Color,
    onCheckedBox: (Boolean) -> Unit
) {
    var isCheckedState by remember {
        mutableStateOf(isChecked)
    }

    Row(
        modifier = Modifier
            .background(statusColor)
            .width(widthElement)
            .padding(
                start = 4.dp,
                top = 4.dp,
                bottom = 4.dp,
                end = 8.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            modifier = Modifier
                .size(25.dp),
            checked = isCheckedState,
            colors = CheckboxDefaults.colors(
                checkedColor = colorResource(id = R.color.orange)
            ),
            onCheckedChange = {
                isCheckedState = it
                onCheckedBox(!isCheckedState)
            }
        )
        Text(
            text = orderStatusDescription,
            fontSize = 14.sp
        )
    }
}
//</editor-fold>

//<editor-fold desc="getStatusColor">
fun getStatusColor(orderStatus: OrderStatus): Color {
    return when (orderStatus) {
        OrderStatus.NEW -> Color.Red.copy(alpha = 0.1f)
        OrderStatus.PROCESSING -> Color.LightGray.copy(alpha = 0.1f)
        OrderStatus.FINISH -> Color.Green.copy(alpha = 0.1f)
        OrderStatus.ACCEPT -> Color.Blue.copy(alpha = 0.1f)
    }
}
//</editor-fold>

