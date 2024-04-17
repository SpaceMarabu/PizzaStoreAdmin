package com.example.pizzastoreadmin.presentation.images.oneimage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.pizzastore.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.presentation.city.cities.ShowToast
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.funs.dropdown.DropDownMenuStates
import com.example.pizzastoreadmin.presentation.funs.dropdown.DropDownTextField
import com.example.pizzastoreadmin.presentation.funs.getOutlinedTextFieldColors
import com.example.pizzastoreadmin.presentation.funs.getScreenWidthDp
import com.example.pizzastoreadmin.presentation.sharedstates.ShouldLeaveScreenState

@Composable
fun OneImageScreen(
    paddingValues: PaddingValues,
    leaveScreen: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: OneImageScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.state.collectAsState()


    when (screenState.value) {

        OneImageScreenState.Initial -> {}

        is OneImageScreenState.Content -> {
            OneImageScreenContent(
                viewModel,
                paddingValues
            ) {
                leaveScreen()
            }
        }

        OneImageScreenState.Loading -> {
            CircularLoading()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneImageScreenContent(
    viewModel: OneImageScreenViewModel,
    paddingValues: PaddingValues,
    leaveScreen : () -> Unit
) {

    val shouldLeaveScreenState by viewModel.shouldLeaveScreenState.collectAsState()
    when (val currentShouldLeaveState = shouldLeaveScreenState) {
        is ShouldLeaveScreenState.Error -> {
            ShowToast(text = currentShouldLeaveState.description)
        }
        ShouldLeaveScreenState.Exit -> leaveScreen()
        ShouldLeaveScreenState.Processing -> {}
    }

    var currentScreenContentStates by remember {
        mutableStateOf(
            CurrentScreenContentStates(
                imageUri = null,
                pictureName = "",
                isTextNotEmpty = true
            )
        )
    }

    var dropDownMenuStates by remember {
        mutableStateOf(
            DropDownMenuStates(
                isProductMenuExpanded = false,
                selectedOptionText = PictureType.PIZZA.type
            )
        )
    }

    val optionsForDropDownMenu = viewModel.getAllPictureTypes()

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            currentScreenContentStates = currentScreenContentStates.copy(imageUri = uri)
        }
    val context = LocalContext.current
    val screenWidthDp = getScreenWidthDp()
    val edgeOfBoxPicture = 300.dp
    val borderStrokeDpPicture = 1.dp
    val paddingBoxPicture = ((screenWidthDp - edgeOfBoxPicture) / 2) - borderStrokeDpPicture

    Scaffold(
        modifier = Modifier
            .padding(bottom = paddingValues.calculateBottomPadding()),
        floatingActionButton = {
            if (currentScreenContentStates.imageUri != null 
                && currentScreenContentStates.pictureName.isNotBlank()) {
                FloatingActionButton(
                    modifier = Modifier
                        .width(100.dp)
                        .border(
                            border = BorderStroke(1.dp, Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                    onClick = {
                        val currentUri = currentScreenContentStates.imageUri
                        if (currentUri != null) {
                            val imageByte = viewModel.readBytes(context, currentUri)
                            if (imageByte != null) {
                                viewModel.putImageToStorage(
                                    name = currentScreenContentStates.pictureName,
                                    type = dropDownMenuStates.selectedOptionText,
                                    imageByteArray =  imageByte
                                )
                            }
                        }
                    }) {
                    Text(
                        text = "DONE",
                        fontSize = 24.sp
                    )
                }
            }
        }
    ) {

        Column {

            TextFieldPicture(currentStates = currentScreenContentStates) {
                currentScreenContentStates = it
            }

            DropDownTextField(
                dropDownMenuStates = dropDownMenuStates,
                options = optionsForDropDownMenu
            ) { menuStatesResult ->
                dropDownMenuStates = menuStatesResult
            }

            Box(
                modifier = Modifier
                    .padding(
                        start = paddingBoxPicture,
                        top = 8.dp,
                        end = paddingBoxPicture
                    )
                    .width(edgeOfBoxPicture)
                    .height(edgeOfBoxPicture)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(
                        border = BorderStroke(borderStrokeDpPicture, Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        launcher.launch("image/*")
                    }
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = rememberAsyncImagePainter(currentScreenContentStates.imageUri),
                    contentDescription = "product image"
                )
            }
        }
    }
}


//<editor-fold desc="TextFieldPicture">
@Composable
fun TextFieldPicture(
    currentStates: CurrentScreenContentStates,
    onTextChanged: (CurrentScreenContentStates) -> Unit
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp
            ),
        isError = !currentStates.isTextNotEmpty,
        label = { Text(text = "PictureName") },
        value = currentStates.pictureName,
        onValueChange = {
            onTextChanged(
                currentStates.copy(
                    isTextNotEmpty = it.isNotBlank(),
                    pictureName = it
                )
            )
        },
        colors = getOutlinedTextFieldColors(),
        shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))
    )
}
//</editor-fold>

















