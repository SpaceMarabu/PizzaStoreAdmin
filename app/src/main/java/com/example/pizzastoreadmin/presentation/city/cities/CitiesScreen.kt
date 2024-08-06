package com.example.pizzastoreadmin.presentation.city.cities

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastore.R
import com.example.pizzastoreadmin.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.funs.DividerList

@Composable
fun CitiesScreen(
    paddingValues: PaddingValues,
    onAddOrCityClicked: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: CitiesScreenUDFVM = viewModel(factory = component.getViewModelFactory())

    val screenState by viewModel.model.collectAsState()

    val currentContext = LocalContext.current
    val deletingSuccessText = stringResource(R.string.success_deleting)
    val deletingFailedText = stringResource(R.string.deleting_failed)

    LaunchedEffect(key1 = Unit) {
        viewModel.labelEvents.collect {
            when (it) {
                LabelEvents.DeleteComplete -> {
                    showToastWarn(currentContext, deletingSuccessText)
                }

                LabelEvents.DeleteFailed -> {
                    showToastWarn(currentContext, deletingFailedText)
                }

                LabelEvents.AddOrEditCity -> {
                    onAddOrCityClicked()
                }
            }
        }
    }


    when (val currentScreenState = screenState.contentState) {

        is CitiesStore.State.ContentState.Content -> {
            ListCitiesScreen(
                cities = currentScreenState.cities,
                selectedCitiesList = screenState.selectedCities,
                buttonState = screenState.buttonState,
                paddingValues = paddingValues,
                onButtonClick = { viewModel.buttonClick() },
                onSelectCity = { viewModel.selectCity(it) },
                onUnselectCity = { viewModel.unselectCity(it) },
                onCityClick = { viewModel.cityClick(it) }
            )
        }

        CitiesStore.State.ContentState.Error -> {}
        CitiesStore.State.ContentState.Initial -> {}
        CitiesStore.State.ContentState.Loading -> {
            CircularLoading()
        }
    }
}

fun showToastWarn(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

//<editor-fold desc="Экран со списком городов">
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListCitiesScreen(
    cities: List<City>,
    selectedCitiesList: List<City>,
    buttonState: CitiesStore.State.ButtonState,
    paddingValues: PaddingValues,
    onButtonClick: () -> Unit,
    onSelectCity: (City) -> Unit,
    onUnselectCity: (City) -> Unit,
    onCityClick: (City) -> Unit
) {

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
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    onButtonClick()
                }
            ) {
                Text(
                    text = when (buttonState) {
                        CitiesStore.State.ButtonState.Add -> {
                            stringResource(R.string.add_button)
                        }

                        CitiesStore.State.ButtonState.Delete -> {
                            stringResource(R.string.delete_button)
                        }
                    },
                    fontSize = 24.sp
                )
            }
        }
    ) {
        LazyColumn {
            items(items = cities, key = { it.id }) { city ->
                CityRow(
                    city = city,
                    onCityClick = {
                        onCityClick(it)
                    },
                    onSelectCity = {
                        onSelectCity(it)
                    },
                    onUnselectCity = {
                        onUnselectCity(it)
                    },
                    selectedCitiesList = selectedCitiesList
                )
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
    selectedCitiesList: List<City>,
    onCityClick: (City) -> Unit,
    onSelectCity: (City) -> Unit,
    onUnselectCity: (City) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCityClick(city)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = city in selectedCitiesList,
            colors = CheckboxDefaults.colors(
                checkedColor = colorResource(id = R.color.orange)
            ),
            onCheckedChange = { state ->
                if (state) {
                    onSelectCity(city)
                } else {
                    onUnselectCity(city)
                }
            }
        )
        Text(
            text = city.name,
            fontSize = 24.sp
        )
    }
}
//</editor-fold>
