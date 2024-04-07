package com.example.pizzastoreadmin.presentation.city.onecity

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastore.R
import com.example.pizzastore.di.getApplicationComponent
import com.example.pizzastoreadmin.presentation.city.onecity.states.OneCityScreenState
import com.example.pizzastoreadmin.presentation.city.onecity.states.PointViewState
import com.example.pizzastoreadmin.presentation.sharedstates.ShouldLeaveScreenState
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.funs.getScreenWidthDp
import kotlinx.coroutines.flow.StateFlow

@Composable
fun OneCityScreen(
    paddingValues: PaddingValues,
    onExitLet: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: OneCityScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.state.collectAsState()


    when (screenState.value) {

        OneCityScreenState.Initial -> {}

        is OneCityScreenState.Content -> {
            OneCityScreenContent(
                paddingValues = paddingValues,
                viewModel = viewModel
            ) {
                onExitLet()
            }
        }

        OneCityScreenState.Loading -> {
            CircularLoading()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OneCityScreenContent(
    paddingValues: PaddingValues,
    viewModel: OneCityScreenViewModel,
    onExitLet: () -> Unit
) {

    val listPoints = viewModel.listPoints.collectAsState()
    val cityState = viewModel.cityState.collectAsState()

    val shouldLeaveScreen = viewModel.shouldLeaveScreenState.collectAsState()

    when (shouldLeaveScreen.value) {
        is ShouldLeaveScreenState.Error -> {
            val currentLeaveState = shouldLeaveScreen.value as ShouldLeaveScreenState.Error
            Toast.makeText(
                LocalContext.current,
                currentLeaveState.description,
                Toast.LENGTH_LONG
            ).show()
        }
        ShouldLeaveScreenState.Exit -> {
            onExitLet()
        }
        ShouldLeaveScreenState.Processing -> {}
    }

    Scaffold (
        modifier = Modifier
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) { _ ->
        LazyColumn {
            item {
                TextFieldCity(
                    label = "Город",
                    textIn = cityState.value.city?.name,
                    needCallbackIn = viewModel.needCallback,
                    isError = !cityState.value.isCityNameIsCorrect
                ) {
                    viewModel.editCityName(it)
                }
            }
            items(
                items = listPoints.value,
                key = { item: PointViewState -> item.point.id }) { pointViewState ->

                val index = listPoints.value.indexOf(pointViewState)
                val point = pointViewState.point

                Column(
                    modifier = Modifier.animateItemPlacement(animationSpec = tween(durationMillis = 500))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp,
                                start = 16.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}.",
                            fontSize = 20.sp
                        )
                        Icon(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(35.dp)
                                .clickable {
                                    viewModel.deletePoint(index)
                                },
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_cross),
                            contentDescription = null
                        )
                    }
                    TextFieldCity(
                        label = "Адрес Пиццерии",
                        textIn = point.address,
                        needCallbackIn = viewModel.needCallback,
                        isError = !pointViewState.isAddressValid
                    ) { text ->
                        viewModel.editPoint(index = index, address = text)
                    }
                    TextFieldCity(
                        label = "Геометка пиццерии",
                        textIn = point.coords,
                        needCallbackIn = viewModel.needCallback,
                        isError = !pointViewState.isGeopointValid
                    ) { text ->
                        viewModel.editPoint(index = index, coords = text)
                    }
                }
            }
            item {
                val screenDp = getScreenWidthDp()
                val halfScreenDp = screenDp / 2

                val paddingBetweenButtons = 16.dp
                val paddingStartEnd = 8.dp

                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = paddingStartEnd, top = 32.dp, end = paddingStartEnd),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    ButtonWithText(
                        modifier = Modifier
                            .padding(end = paddingBetweenButtons / 2),
                        width = halfScreenDp - (paddingBetweenButtons / 2) - paddingStartEnd,
                        text = "Добавить точку"
                    ) {
                        viewModel.getNewPoint()
                    }
                    ButtonWithText(
                        modifier = Modifier
                        .padding(start = paddingBetweenButtons),
                        width = halfScreenDp - (paddingBetweenButtons / 2) - paddingStartEnd,
                        text = "Готово"
                    ) {
                        viewModel.exitScreen()
                    }
                }
            }
        }
    }
}

//<editor-fold desc="Кнопка">
@Composable
fun ButtonWithText(
    modifier: Modifier = Modifier,
    text: String,
    width: Dp,
    color: Color = Color.Black,
    onButtonClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .width(width)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .clickable {
                onButtonClicked()
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
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


//<editor-fold desc="Поле ввода текста">
@Composable
fun TextFieldCity(
    label: String,
    textIn: String? = "",
    isError: Boolean,
    needCallbackIn: StateFlow<Boolean>,
    modifier: Modifier = Modifier,
    textResult: (String) -> Unit
) {

    val needCallback = needCallbackIn.collectAsState()

    var text by remember(textIn) { mutableStateOf(textIn ?: "") }
    var errorState by remember(isError) {
        mutableStateOf(isError)
    }

    if (needCallback.value) {
        textResult(text)
    }

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp
            )
        ,
        isError = errorState,
        label = { Text(text = label) },
        value = text,
        onValueChange = {
            errorState = false
            text = it
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = Color.LightGray,
            unfocusedLabelColor = Color.LightGray,
            backgroundColor = Color.White,
            textColor = Color.Black,
            focusedBorderColor = Color.Black,
            focusedLabelColor = Color.Black,
            cursorColor = Color.Gray
        ),
        shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))
    )
}
//</editor-fold>