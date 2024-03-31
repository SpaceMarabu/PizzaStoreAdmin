package com.example.pizzastoreadmin.presentation.city

import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
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
import com.example.pizzastore.domain.entity.Point
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.presentation.funs.CircularLoading

@Composable
fun CityScreen() {

    val component = getApplicationComponent()
    val viewModel: CityScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.state.collectAsState()


    when (screenState.value) {

        is CityScreenState.Initial -> {}

        is CityScreenState.ListCities -> {
            val currentScreenState = screenState.value as CityScreenState.ListCities
            ListCitiesScreen(
                cities = currentScreenState.cities,
                viewModel = viewModel
            )
        }

        is CityScreenState.OneCity -> {
            val currentScreenState = screenState.value as CityScreenState.OneCity
            OneCityScreen(
                currentScreenState.city,
                viewModel
            )
        }

        CityScreenState.Loading -> {
            CircularLoading()
        }
    }
}

//<editor-fold desc="Экран со списком городов">
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListCitiesScreen(
    cities: List<City>,
    viewModel: CityScreenViewModel
) {
    val citiesToDelete: MutableSet<City> = remember {
        mutableSetOf()
    }
    var isCitiesToDeleteEmpty by remember {
        mutableStateOf(true)
    }

    var isButtonClicked by remember {
        mutableStateOf(false)
    }
    if (isButtonClicked) {
        if (!isCitiesToDeleteEmpty) {
            SideEffect {
                viewModel.deleteCity(citiesToDelete.toList())
            }
        } else {
            SideEffect {
                viewModel.changeScreenState(CityScreenState.OneCity())
            }
        }
        isButtonClicked = false
    }

    Scaffold(
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
                    isButtonClicked = true
                }) {
                Text(
                    text = if (isCitiesToDeleteEmpty) "ADD" else "DELETE",
                    fontSize = 24.sp
                )
            }
        }
    ) {
        LazyColumn {
            items(items = cities, key = { it.id }) { city ->
                CityRow(city = city) { isBoxChecked ->
                    if (isBoxChecked) {
                        citiesToDelete.add(city)
                    } else {
                        citiesToDelete.removeIf { it.id == city.id }
                    }
                    isCitiesToDeleteEmpty = citiesToDelete.size == 0
                }
                DividerList()
            }
        }
    }

}
//</editor-fold>


//<editor-fold desc="Строка с чекбоксом">
@Composable
fun CityRow(
    city: City,
    onCheckboxChanged: (Boolean) -> Unit
) {
    var isCheckedCity by remember {
        mutableStateOf(false)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isCheckedCity,
            onCheckedChange = {
                isCheckedCity = it
                onCheckboxChanged(isCheckedCity)
            }
        )
        Text(
            text = city.name,
            fontSize = 24.sp
        )
    }
}
//</editor-fold>


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OneCityScreen(
    city: City?,
    viewModel: CityScreenViewModel
) {
    val listPoints = viewModel.listPoints.collectAsState()

    var isScreenInited by remember {
        mutableStateOf(false)
    }
    if (!isScreenInited) {
        isScreenInited = true
        viewModel.initListPoints(city?.points ?: listOf())
    }

    Scaffold { paddingValues ->
        LazyColumn {
            item {
                TextFieldCity(
                    label = "Город",
                    textIn = city?.name
                ) {

                }
            }
            items(
                items = listPoints.value.points,
                key = { item: Point -> item.id }) { pointFromList ->

                val index = listPoints.value.points.indexOf(pointFromList)

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
                        textIn = pointFromList.address
                    ) { text ->
                        viewModel.editPoint(index = index, address = text)
                    }
                    TextFieldCity(
                        label = "Геометка пиццерии",
                        textIn = pointFromList.coords
                    ) { text ->
                        viewModel.editPoint(index = index, coords = text)
                        Log.d("TEST_TEST", "rec")
                    }
                }
            }
            item {
                val displayMetrics: DisplayMetrics = LocalContext.current.resources.displayMetrics
                val dpWidth = displayMetrics.widthPixels / displayMetrics.density
                val halfScreenDp = (dpWidth / 2).dp
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

                    }
                }

            }
        }
    }
}

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


@Composable
fun TextFieldCity(
    label: String,
    textIn: String? = "",
    modifier: Modifier = Modifier,
    textResult: (String) -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: CityScreenViewModel = viewModel(factory = component.getViewModelFactory())
    val needCallback = viewModel.needCallback.collectAsState()


    var text by remember(textIn) { mutableStateOf(textIn ?: "") }

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
//            .onFocusChanged { if (!it.hasFocus && text.isNotBlank()) textResult(text) }
        ,
        label = { androidx.compose.material.Text(text = label) },
        value = text,
        onValueChange = {
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