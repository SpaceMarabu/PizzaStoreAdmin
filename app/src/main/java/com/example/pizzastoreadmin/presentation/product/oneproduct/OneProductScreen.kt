package com.example.pizzastoreadmin.presentation.product.oneproduct

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.pizzastore.R
import com.example.pizzastore.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.ProductType
import com.example.pizzastoreadmin.navigation.Screen
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.funs.dropdown.DropDownMenuStates
import com.example.pizzastoreadmin.presentation.funs.dropdown.DropDownTextField
import com.example.pizzastoreadmin.presentation.funs.getScreenWidthDp
import com.example.pizzastoreadmin.presentation.product.oneproduct.states.EditType
import com.example.pizzastoreadmin.presentation.product.oneproduct.states.OneProductScreenState
import com.example.pizzastoreadmin.presentation.sharedstates.ShouldLeaveScreenState

@Composable
fun OneProductScreen(
    paddingValues: PaddingValues,
    photoUriString: String?,
    needPhotoUri: () -> Unit,
    onExitLet: () -> Unit
) {

    val component = getApplicationComponent()
    val viewModel: OneProductScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState = viewModel.state.collectAsState()


    when (screenState.value) {

        OneProductScreenState.Initial -> {}

        is OneProductScreenState.Content -> {
            OneProductScreenContent(
                paddingValues = paddingValues,
                viewModel = viewModel,
                photoUriString = photoUriString,
                needPhotoUri = {
                    needPhotoUri()
                }
            ) {
                onExitLet()
            }
        }

        OneProductScreenState.Loading -> {
            CircularLoading()
        }
    }
}

//<editor-fold desc="OneProductScreenContent">
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneProductScreenContent(
    paddingValues: PaddingValues,
    viewModel: OneProductScreenViewModel,
    photoUriString: String?,
    needPhotoUri: () -> Unit,
    onExitLet: () -> Unit
) {

    val shouldLeaveScreen = viewModel.shouldLeaveScreenState.collectAsState()
    val currentProductState by viewModel.currentProduct.collectAsState()
    val needCallback by viewModel.needCallback.collectAsState()

    val screenWidthDp = getScreenWidthDp()
    val edgeOfBoxPicture = 300.dp
    val borderStrokeDpPicture = 1.dp
    val paddingBoxPicture = ((screenWidthDp - edgeOfBoxPicture) / 2) - borderStrokeDpPicture

    val imageSource =
        (
                if (
                    (photoUriString != null && photoUriString.contains(Screen.KEY_URI_STRING))
                    || photoUriString == null
                )
                    currentProductState.product.photo
                        ?: R.drawable.pic_hungry_cat
                else
                    photoUriString
                )

    val request = ImageRequest
        .Builder(LocalContext.current)
        .data(imageSource)
        .size(coil.size.Size.ORIGINAL)
        .build()

    val painter = rememberAsyncImagePainter(
        model = request
    )

    var dropDownMenuStates by remember {
        mutableStateOf(
            DropDownMenuStates(
                isProductMenuExpanded = false,
                selectedOption = currentProductState.product.type
            )
        )
    }

    if (needCallback) {
        viewModel.editProduct(EditType.PHOTO, imageSource.toString())
        viewModel.editProduct(dropDownMenuStates.selectedOption as ProductType)
    }

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

    Scaffold(
        modifier = Modifier
            .padding(bottom = paddingValues.calculateBottomPadding()),
        floatingActionButton = {
            if (
                currentProductState.isNameValid
                && currentProductState.isPriceValid
                && currentProductState.isPhotoIsNotEmpty
            ) {
                FloatingActionButton(
                    modifier = Modifier
                        .width(100.dp)
                        .border(
                            border = BorderStroke(1.dp, Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                    onClick = {
                        viewModel.exitScreen()
                    }) {
                    Text(
                        text = stringResource(id = R.string.done_button),
                        fontSize = 24.sp
                    )
                }
            }
        }
    ) { _ ->
        Column {

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
                        needPhotoUri()
                        viewModel.needCallbackScreen()
                    }
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = "product image"
                )
            }

            DropDownTextField(
                dropDownMenuStates = dropDownMenuStates,
                options = viewModel.getAllProductTypes()
            ) { menuStatesResult ->
                dropDownMenuStates = menuStatesResult
            }

            TextFieldProduct(
                label = "Name",
                textIn = currentProductState.product.name,
                needCallback = needCallback,
                isError = !currentProductState.isNameValid
            ) {
                viewModel.editProduct(EditType.NAME, it)
            }

            TextFieldProduct(
                label = "Price",
                textIn = currentProductState.product.price.toString(),
                needCallback = needCallback,
                isError = !currentProductState.isPriceValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            ) {
                viewModel.editProduct(EditType.PRICE, it)
            }

            TextFieldProduct(
                label = "Description",
                textIn = currentProductState.product.description,
                needCallback = needCallback
            ) {
                viewModel.editProduct(EditType.DESCRIPTION, it)
            }

        }
    }
}
//</editor-fold>

//<editor-fold desc="Поле ввода текста">
@Composable
fun TextFieldProduct(
    modifier: Modifier = Modifier,
    label: String,
    textIn: String? = "",
    isError: Boolean = false,
    needCallback: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textResult: (String) -> Unit
) {

    var text by remember(textIn) { mutableStateOf(textIn ?: "") }
    var errorState by remember(isError) {
        mutableStateOf(isError)
    }

    if (needCallback) {
        textResult(text)
    }

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp
            ),
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
        shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp)),
        keyboardOptions = keyboardOptions
    )
}
//</editor-fold>