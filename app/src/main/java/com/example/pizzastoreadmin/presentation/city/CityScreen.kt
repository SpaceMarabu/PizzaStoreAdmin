package com.example.pizzastoreadmin.presentation.city

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneCityScreen(
    city: City?,
    viewModel: CityScreenViewModel
) {
    val listPoints: MutableState<List<Point>> = remember {
        mutableStateOf(city?.points ?: listOf())
    }
    var pointInputsIsNotEmpty by remember {
        mutableStateOf(true)
    }
    Scaffold {
        LazyColumn {
            item {
                TextFieldCity(label = city?.name ?: "Город")
                DividerList()
            }
            items(items = listPoints.value, key = { it.id }) {
                Text(
                    modifier = Modifier
                        .padding(
                            top = 8.dp,
                            start = 16.dp
                        ),
                    text = "${it.id}.",
                    fontSize = 16.sp
                )
                CallbackableTextFieldCity(label = "Адрес Пиццерии") { text ->
                    it
                }
                CallbackableTextFieldCity(label = "Геометка пиццерии") { isGeopointInputed ->
//                    pointInputsIsNotEmpty = pointInputsIsNotEmpty && isGeopointInputed
                }
            }
            item {
                val listPointValue = listPoints.value
                if (
                    listPointValue.isEmpty() ||
                    (listPointValue.last().address.isNotBlank()
                            && listPointValue.last().coords.isNotBlank())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 8.dp,
                                end = 8.dp,
                                top = 32.dp
                            )
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black)
                            .clickable {
                                listPoints.value =
                                    listPoints.value + viewModel.getNewPoint(listPoints.value)
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Добавить точку",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
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

//<editor-fold desc="Текст филд без колбека">
@Composable
fun TextFieldCity(
    label: String,
    modifier: Modifier = Modifier
) {
    CallbackableTextFieldCity(
        label = label,
        modifier = modifier
    ) {}
}
//</editor-fold>

@Composable
fun CallbackableTextFieldCity(
    label: String,
    modifier: Modifier = Modifier,
    onInputIsNotEmpty: (String) -> Unit
) {

    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp
            ),
        label = { androidx.compose.material.Text(text = label) },
        value = text ?: "",
        onValueChange = {
            onInputIsNotEmpty(text)
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