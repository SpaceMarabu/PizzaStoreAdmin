package com.example.pizzastoreadmin.presentation.city.cities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastore.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.presentation.funs.CircularLoading

@Composable
fun CitiesScreen(
    onAddOrCityClicked: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: CitiesScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.state.collectAsState()


    when (screenState.value) {

        is CitiesScreenState.Initial -> {}

        is CitiesScreenState.ListCities -> {
            val currentScreenState = screenState.value as CitiesScreenState.ListCities
            ListCitiesScreen(
                cities = currentScreenState.cities,
                viewModel = viewModel
            ) {
                onAddOrCityClicked()
            }
        }

        CitiesScreenState.Loading -> {
            CircularLoading()
        }
    }
}

//<editor-fold desc="Экран со списком городов">
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListCitiesScreen(
    cities: List<City>,
    viewModel: CitiesScreenViewModel,
    onAddOrCityClicked: () -> Unit
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
            onAddOrCityClicked()
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
