package com.example.pizzastoreadmin.presentation.city.onecity

import android.annotation.SuppressLint
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastoreadmin.R
import com.example.pizzastoreadmin.domain.entity.Point
import com.example.pizzastoreadmin.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.funs.getOutlinedColors
import com.example.pizzastoreadmin.presentation.funs.getScreenWidthDp
import com.example.pizzastoreadmin.presentation.funs.showToastWarn

@Composable
fun OneCityScreen(
    paddingValues: PaddingValues,
    onExitLet: () -> Unit
) {

    val component = getApplicationComponent()
    val updater: OneCityScreenUpdater = viewModel(factory = component.getViewModelFactory())

    val model by updater.model.collectAsState()

    val editingFailedText = stringResource(id = R.string.failed)
    val currentContext = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        updater.labelEvents.collect {
            when (it) {

                LabelEvents.ErrorRepositoryResponse -> {
                    showToastWarn(currentContext, editingFailedText)
                }

                LabelEvents.Exit -> {
                    onExitLet()
                }
            }
        }
    }


    when (val contentState = model.contentState) {

        is OneCityStore.State.ContentState.Content -> {
            OneCityScreenContent(
                paddingValues = paddingValues,
                city = contentState.city,
                cityNameFieldValid = model.textFieldValuesValid.cityNameCorrect,
                pointsFieldValid = model.textFieldValuesValid.pointsValuesCorrect,
                onRemoveClick = { updater.removePointClick(it) },
                onDoneClick = { updater.doneClick()},
                onEditCityName = { updater.changeCityName(it) },
                onEditPoint = { updater.changePoint(it) },
                onGetNewPointClick = { updater.addPointClick() }
            )
        }

        OneCityStore.State.ContentState.Error -> {}
        OneCityStore.State.ContentState.Initial -> {}
        OneCityStore.State.ContentState.Loading -> {
            CircularLoading()
        }
    }
}


//<editor-fold desc="OneCityScreenContent">
@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OneCityScreenContent(
    paddingValues: PaddingValues,
    city: City,
    cityNameFieldValid: Boolean,
    pointsFieldValid: List<Boolean>,
    onEditCityName: (String) -> Unit,
    onEditPoint: (Point) -> Unit,
    onGetNewPointClick: () -> Unit,
    onRemoveClick: (Point) -> Unit,
    onDoneClick: () -> Unit
) {

    val screenDp = getScreenWidthDp()
    val halfScreenDp = screenDp / 2

    val paddingBetweenButtons = 16.dp
    val paddingStartEnd = 8.dp

    Scaffold(
        modifier = Modifier
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        LazyColumn {
            item {
                TextFieldCity(
                    label = stringResource(R.string.city_label),
                    textIn = city.name,
                    isError = !cityNameFieldValid
                ) {
                    onEditCityName(it)
                }
            }
            items(
                items = city.points,
                key = { it.id }) { point ->

                Column(
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = tween(
                            durationMillis = 500
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp,
                                start = 16.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${point.id}.",
                            fontSize = 20.sp
                        )
                        Icon(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(25.dp)
                                .clickable {
                                    onRemoveClick(point)
                                },
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_cross),
                            contentDescription = null
                        )
                    }
                    TextFieldCity(
                        label = stringResource(R.string.pizza_store_address),
                        textIn = point.address,
                        isError = !pointsFieldValid[point.id - 1]
                    ) { text ->
                        onEditPoint(point.copy(address = text))
                    }
                    TextFieldCity(
                        label = stringResource(R.string.pizza_store_geocode),
                        textIn = point.coords,
                        isError = !pointsFieldValid[point.id - 1]
                    ) { text ->
                        onEditPoint(point.copy(coords = text))
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = paddingStartEnd, top = 32.dp, end = paddingStartEnd),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    ButtonWithText(
                        modifier = Modifier
                            .padding(end = paddingBetweenButtons / 2),
                        width = halfScreenDp - (paddingBetweenButtons / 2) - paddingStartEnd,
                        text = stringResource(R.string.add_point_button)
                    ) {
                        onGetNewPointClick()
                    }
                    ButtonWithText(
                        modifier = Modifier
                            .padding(start = paddingBetweenButtons),
                        width = halfScreenDp - (paddingBetweenButtons / 2) - paddingStartEnd,
                        text = stringResource(R.string.done_button)
                    ) {
                        onDoneClick()
                    }
                }
            }
        }
    }
}
//</editor-fold>

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

//<editor-fold desc="Поле ввода текста">
@Composable
fun TextFieldCity(
    modifier: Modifier = Modifier,
    label: String,
    textIn: String = "",
    isError: Boolean,
    textResult: (String) -> Unit
) {

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp
            ),
        isError = isError,
        label = { Text(text = label) },
        value = textIn,
        onValueChange = {
            textResult(it)
        },
        colors = getOutlinedColors(),
        shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))
    )
}
//</editor-fold>