package com.example.pizzastoreadmin.presentation.product.oneproduct

import android.annotation.SuppressLint
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.pizzastoreadmin.R
import com.example.pizzastoreadmin.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.Product
import com.example.pizzastoreadmin.domain.entity.ProductType
import com.example.pizzastoreadmin.navigation.Screen
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.funs.DropDownTextField
import com.example.pizzastoreadmin.presentation.funs.getOutlinedColors
import com.example.pizzastoreadmin.presentation.funs.getScreenWidthDp
import com.example.pizzastoreadmin.presentation.funs.showToastWarn

@Composable
fun OneProductScreen(
    paddingValues: PaddingValues,
    photoUriString: String?,
    needPhotoUri: () -> Unit,
    onExitLet: () -> Unit
) {

    val component = getApplicationComponent()
    val updater: OneProductScreenUpdater = viewModel(factory = component.getViewModelFactory())

    val model by updater.model.collectAsState()

    val currentContext = LocalContext.current
    val deletingFailedText = stringResource(R.string.failed)

    LaunchedEffect(key1 = Unit) {
        updater.labelEvents.collect {
            when (it) {
                LabelEvent.ErrorRepositoryResponse -> {
                    showToastWarn(currentContext, deletingFailedText)
                }

                LabelEvent.ExitScreen -> {
                    onExitLet()
                }

                LabelEvent.PictureClick -> {
                    needPhotoUri()
                }
            }
        }
    }


    when (val contentState = model.contentState) {

        is OneProductStore.State.ContentState.Content -> {
            OneProductScreenContent(
                paddingValues = paddingValues,
                product = contentState.product,
                listProductTypes = model.listProductTypes,
                isDropDownExpanded = model.isDropDownExpanded,
                photoUriString = photoUriString,
                onPictureClick = {
                    updater.clickPicture()
                },
                onDoneClick = { updater.doneClick() },
                onDropDownClick = { updater.clickDropDown() },
                onProductTypeSelected = { updater.clickType(it) },
                onNameChange = { updater.changeProductName(it) },
                onPriceChange = { updater.changeProductPrice(it) },
                onDescriptionChange = { updater.changeDescription(it) },
                onScreenClick = { updater.screenClick() }
            )
        }

        OneProductStore.State.ContentState.Error -> {}
        OneProductStore.State.ContentState.Initial -> {}
        OneProductStore.State.ContentState.Loading -> {
            CircularLoading()
        }
    }
}

//<editor-fold desc="OneProductScreenContent">
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OneProductScreenContent(
    paddingValues: PaddingValues,
    product: Product,
    listProductTypes: List<ProductType>,
    isDropDownExpanded: Boolean,
    photoUriString: String?,
    onPictureClick: () -> Unit,
    onDoneClick: () -> Unit,
    onDropDownClick: () -> Unit,
    onProductTypeSelected: (ProductType) -> Unit,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onScreenClick: () -> Unit
) {

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
                    product.photo
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
                    onDoneClick()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = stringResource(id = R.string.done_button),
                    fontSize = 24.sp
                )
            }
        }
    ) {
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
                        onPictureClick()
                    }
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = "product image"
                )
            }

            DropDownTextField(
                isDropDownExpanded = isDropDownExpanded,
                options = listProductTypes,
                selectedOption = product.type,
                onOptionClicked = { onProductTypeSelected(it as ProductType) },
                onDropDownClicked = { onDropDownClick() },
                onScreenClicked = { onScreenClick() }
            )

            TextFieldProduct(
                label = "Name",
                textIn = product.name
            ) {
                onNameChange(it)
            }

            TextFieldProduct(
                label = "Price",
                textIn = product.price.toString(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            ) {
                onPriceChange(it)
            }

            TextFieldProduct(
                label = "Description",
                textIn = product.description,
            ) {
                onDescriptionChange(it)
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
    textIn: String = "",
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
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
        shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp)),
        keyboardOptions = keyboardOptions
    )
}
//</editor-fold>