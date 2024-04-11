package com.example.pizzastoreadmin.presentation.images.images

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pizzastore.di.getApplicationComponent
import com.example.pizzastoreadmin.presentation.funs.CircularLoading

@Composable
fun ImagesScreen(
    paddingValues: PaddingValues,
    leaveScreen: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: ImagesScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.state.collectAsState()


    when (screenState.value) {

        ImagesScreenState.Initial -> {}

        is ImagesScreenState.Content -> {
            viewModel.getListPictures()
        }

        ImagesScreenState.Loading -> {
            CircularLoading()
        }
    }
}