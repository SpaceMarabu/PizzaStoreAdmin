package com.example.pizzastoreadmin.presentation.images

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.pizzastore.di.getApplicationComponent
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.funs.getScreenWidthDp

@Composable
fun OneImageScreen(
    paddingValues: PaddingValues,
    onExitLet: () -> Unit
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
            )
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
    paddingValues: PaddingValues
) {
    var currentStates by remember {
        mutableStateOf(
            CurrentStates(
                imageUri = null,
                pictureName = "",
                isTextNotEmpty = true,
                isErrorInTextField = false
            )
        )
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            currentStates = currentStates.copy(imageUri = uri)
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
            if (currentStates.imageUri != null && currentStates.pictureName.isNotBlank()) {
                FloatingActionButton(
                    modifier = Modifier
                        .width(100.dp)
                        .border(
                            border = BorderStroke(1.dp, Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                    onClick = {
                        val currentUri = currentStates.imageUri
                        if (currentUri != null) {
                            val imageByte = viewModel.readBytes(context, currentUri)
                            if (imageByte != null) {
                                viewModel.putImageToStorage(
                                    imageByte,
                                    currentStates.pictureName
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
            TextFieldPicture(currentStates = currentStates) {
                currentStates = it
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
                    painter = rememberAsyncImagePainter(currentStates.imageUri),
                    contentDescription = "product image"
                )
            }
        }
    }
}

//<editor-fold desc="TextFieldPicture">
@Composable
fun TextFieldPicture(
    currentStates: CurrentStates,
    onTextChanged: (CurrentStates) -> Unit
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















