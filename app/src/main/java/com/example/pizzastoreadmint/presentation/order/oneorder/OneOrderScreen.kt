package com.example.pizzastoreadmint.presentation.order.oneorder

import android.annotation.SuppressLint
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.pizzastoreadmint.R
import com.example.pizzastoreadmint.di.getApplicationComponent
import com.example.pizzastoreadmint.domain.entity.Order
import com.example.pizzastoreadmint.domain.entity.OrderStatus
import com.example.pizzastoreadmint.domain.entity.Product
import com.example.pizzastoreadmint.presentation.funs.CircularLoading
import com.example.pizzastoreadmint.presentation.funs.getScreenWidthDp
import com.example.pizzastoreadmint.presentation.order.utils.LabelEvents
import com.example.pizzastoreadmint.presentation.order.utils.getStatusColor

@Composable
fun OneOrderScreen(
    paddingValues: PaddingValues,
    leaveScreen: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: OneOrderScreenUpdater = viewModel(factory = component.getViewModelFactory())

    LaunchedEffect(key1 = Unit) {
        viewModel.labelEvents.collect {
            when(it) {
                LabelEvents.LeaveScreen -> {
                    leaveScreen()
                }
            }
        }
    }

    val model by viewModel.model.collectAsState()


    when (val currentState = model.screenState) {
        is OneOrderStore.State.OneOrderState.Content -> {
            Content(
                paddingValues = paddingValues,
                order = currentState.order,
                statusMenuExpanded = currentState.isStatusExpanded,
                onStatusClicked = {
                    viewModel.onStatusClick()
                },
                onStatusItemClicked = {
                    viewModel.onStatusItemClick(it)
                },
                onStatusNeedToClose = {
                    viewModel.onStatusNeedClose()
                },
                onDoneClicked = {
                    viewModel.onDoneClick()
                }
            )
        }

//        OneOrderStore.State.OneOrderState.EditingSuccess -> {
//            leaveScreen()
//        }

        OneOrderStore.State.OneOrderState.Loading -> {
            CircularLoading()
        }

        else -> {}
    }

}

//<editor-fold desc="Content">
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    paddingValues: PaddingValues,
    order: Order,
    statusMenuExpanded: Boolean,
    onStatusClicked: () -> Unit,
    onStatusItemClicked: (OrderStatus) -> Unit,
    onStatusNeedToClose: () -> Unit,
    onDoneClicked: () -> Unit
) {

    val thirdScreenWidth = getScreenWidthDp() / 3

    val statuses = listOf(
        OrderStatus.NEW,
        OrderStatus.PROCESSING,
        OrderStatus.FINISH,
        OrderStatus.ACCEPT
    )

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
                onStatusNeedToClose()
                onDoneClicked()
            }
        }
    ) {
        Box {
            RowOrderNumberWithStatus(
                widthMenu = thirdScreenWidth,
                order = order,
                statusButtonShape = statusButtonShape,
                currentOrderState = order.status,
                statusMenuExpanded = statusMenuExpanded,
                statuses = statuses,
                onNewStatusClicked = { status ->
                    onStatusItemClicked(status)
                },
                onStatusClicked = {
                    onStatusClicked()
                }
            )
            Column {
                Spacer(modifier = Modifier.height(100.dp))
                LazyBucket(
                    products = products,
                    bucket = bucket
                ) {
                    onStatusNeedToClose()
                }
            }
        }

    }
}
//</editor-fold>

//<editor-fold desc="RowOrderNumberWithStatus">
@Composable
private fun RowOrderNumberWithStatus(
    widthMenu: Dp,
    order: Order,
    statusButtonShape: Shape,
    currentOrderState: OrderStatus,
    statusMenuExpanded: Boolean,
    statuses: List<OrderStatus>,
    onStatusClicked: () -> Unit,
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
                    onStatusClicked()
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
private fun StatusesMenu(
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
private fun LazyBucket(
    products: List<Product>,
    bucket: Map<Product, Int>,
    onDragList: () -> Unit
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
private fun DoneButton(onClick: () -> Unit) {
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
            onClick()
        }) {
        Text(
            text = stringResource(R.string.done_button),
            fontSize = 24.sp
        )
    }
}
//</editor-fold>









