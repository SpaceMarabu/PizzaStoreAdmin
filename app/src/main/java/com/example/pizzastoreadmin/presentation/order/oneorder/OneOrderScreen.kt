package com.example.pizzastoreadmin.presentation.order.oneorder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastore.R
import com.example.pizzastoreadmin.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.Order
import com.example.pizzastoreadmin.domain.entity.OrderStatus
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.funs.getScreenWidthDp
import com.example.pizzastoreadmin.presentation.order.sharedfun.getStatusColor
import com.example.pizzastoreadmin.presentation.sharedstates.ShouldLeaveScreenState

@Composable
fun OneOrderScreen(
    paddingValues: PaddingValues,
    leaveScreen: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: OneOrderScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.screenState.collectAsState()
    val callbackScreenState = viewModel.shouldLeaveScreenState.collectAsState()

    when (callbackScreenState.value) {

        ShouldLeaveScreenState.Exit -> {
            leaveScreen()
        }

        else -> {}
    }

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

@OptIn(ExperimentalMaterial3Api::class)
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
    val bucket = order.bucket.order
    val products = bucket.keys.toList()

    val statusButtonShape = if (statusMenuExpanded) {
        RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
    } else {
        RoundedCornerShape(10.dp)
    }

    Scaffold(
        modifier = Modifier
            .padding(
                top = 8.dp,
                bottom = paddingValues.calculateBottomPadding(),
                start = 8.dp,
                end = 8.dp
            ),
        floatingActionButton = {
            DoneButton {
                viewModel.editOrderStatus(currentOrderState)
                statusMenuExpanded = false
            }
        }
    ) {
        Box {
            RowOrderNumberWithStatus(
                widthMenu = thirdScreenWidth,
                order = order,
                statusButtonShape = statusButtonShape,
                currentOrderState = currentOrderState,
                statusMenuExpanded = statusMenuExpanded,
                statuses = statuses,
                onNewStatusClicked = { status ->
                    currentOrderState = status
                },
                onStatusClicked = { statusMenuExpandedCallback ->
                    statusMenuExpanded = statusMenuExpandedCallback
                }
            )
            Column {
                Spacer(modifier = Modifier.height(100.dp))
                LazyBucket(
                    products = products,
                    bucket = bucket
                ) {
                    statusMenuExpanded = false
                }
            }
        }

    }
}

//<editor-fold desc="RowOrderNumberWithStatus">
@Composable
fun RowOrderNumberWithStatus(
    widthMenu: Dp,
    order: Order,
    statusButtonShape: Shape,
    currentOrderState: OrderStatus,
    statusMenuExpanded: Boolean,
    statuses: List<OrderStatus>,
    onStatusClicked: (Boolean) -> Unit,
    onNewStatusClicked: (OrderStatus) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "№ ${order.id}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .width(widthMenu)
                .clip(statusButtonShape)
                .background(getStatusColor(currentOrderState))
                .clickable {
                    onStatusClicked(!statusMenuExpanded)
                },
        ) {
            Text(
                modifier = Modifier
                    .padding(
                        start = 4.dp,
                        top = 4.dp,
                        bottom = 4.dp,
                        end = 8.dp
                    ),
                text = currentOrderState.name,
                fontSize = 16.sp
            )
        }

    }
    if (statusMenuExpanded) {
        StatusesMenu(
            widthMenu = widthMenu,
            statuses = statuses.filter { it.ordinal != currentOrderState.ordinal }
        ) {
            onNewStatusClicked(it)
        }
    }
}
//</editor-fold>

//<editor-fold desc="FilterMenu">
@Composable
fun StatusesMenu(
    widthMenu: Dp,
    statuses: List<OrderStatus>,
    onStatusClicked: (OrderStatus) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = 30.dp)
            .zIndex(2f),
        horizontalArrangement = Arrangement.End
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .width(widthMenu)
                .clip(
                    RoundedCornerShape(
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
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

//<editor-fold desc="LazyBucket">
@Composable
fun LazyBucket(
    products: List<Product>,
    bucket: Map<Product, Int>,
    onDragList : () -> Unit
) {
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y != 0f) {
                    onDragList()
                }
                return Offset.Zero
            }
        }
    }

    Text(
        text = stringResource(R.string.bucket_string),
        fontSize = 16.sp,
        textDecoration = TextDecoration.Underline
    )
    LazyColumn(
        modifier = Modifier
            .padding(top = 8.dp)
            .nestedScroll(nestedScrollConnection)
    ) {
        itemsIndexed(products) { position, product ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${position + 1}. ${product.name}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = "${bucket[product].toString()} шт.", fontSize = 20.sp)
            }
        }
    }
}
//</editor-fold>

//<editor-fold desc="DoneButton">
@Composable
fun DoneButton(onClick : () -> Unit) {
    FloatingActionButton(
        modifier = Modifier
            .width(100.dp)
            .border(
                border = BorderStroke(1.dp, Color.Black),
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        onClick = {
            onClick()
        }) {
        Text(
            text = stringResource(R.string.done_button),
            fontSize = 24.sp
        )
    }
}
//</editor-fold>









