package com.example.pizzastoreadmin.presentation.city.cities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
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
    paddingValues: PaddingValues,
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
                viewModel = viewModel,
                paddingValues = paddingValues
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
    paddingValues: PaddingValues,
    onAddOrCityClicked: () -> Unit
) {
    val stateHolder = remember {
        mutableStateOf(CurrentStates(
            citiesToDelete = mutableSetOf(),
            isCitiesToDeleteEmpty = true,
            isButtonClicked = false,
            isItemClicked = false,
            currentCity = null
        ))

    }
    val currentStateValue = stateHolder.value

    if (currentStateValue.isButtonClicked) {
        if (!currentStateValue.isCitiesToDeleteEmpty) {
            SideEffect {
                viewModel.deleteCity(currentStateValue.citiesToDelete.toList())
            }
        } else {
            SideEffect {
                viewModel.setCurrentCity()
            }
            onAddOrCityClicked()
        }
        stateHolder.value = currentStateValue.copy(isButtonClicked = false)
    }
    if (currentStateValue.isItemClicked) {
        SideEffect {
            viewModel.setCurrentCity(currentStateValue.currentCity)
        }
        onAddOrCityClicked()
        stateHolder.value = currentStateValue.copy(isItemClicked = false)
    }


    Scaffold(
        modifier = Modifier
            .padding(bottom = paddingValues.calculateBottomPadding()),
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
                    stateHolder.value = currentStateValue.copy(isButtonClicked = true)
                }) {
                Text(
                    text = if (currentStateValue.isCitiesToDeleteEmpty) "ADD" else "DELETE",
                    fontSize = 24.sp
                )
            }
        }
    ) {
        LazyColumn {
            items(items = cities, key = { it.id }) { city ->
                CityRow(
                    city = city,
                    onClick = {
                        stateHolder.value = currentStateValue
                            .copy(
                                isItemClicked = true,
                                currentCity = city
                            )
                    }
                ) { isBoxChecked ->
                    if (isBoxChecked) {
                        currentStateValue.citiesToDelete.add(city)
                    } else {
                        currentStateValue.citiesToDelete.removeIf { it.id == city.id }
                    }
                    stateHolder.value = currentStateValue
                        .copy(
                            citiesToDelete = currentStateValue.citiesToDelete,
                            isCitiesToDeleteEmpty = currentStateValue.citiesToDelete.size == 0
                        )
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
    onClick: () -> Unit,
    onCheckboxChanged: (Boolean) -> Unit
) {
    var isCheckedCity by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .clickable {
                onClick()
            },
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
