package com.example.pizzastoreadmin.presentation.images.images

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.pizzastore.R
import com.example.pizzastoreadmin.di.getApplicationComponent
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.presentation.funs.CircularLoading
import com.example.pizzastoreadmin.presentation.funs.getScreenWidthDp

@Composable
fun ImagesScreen(
    paddingValues: PaddingValues,
    addImageClicked: () -> Unit,
    exitScreen: (uriString: String) -> Unit
) {

    val component = getApplicationComponent()
    val updater: ImagesScreenUpdater = viewModel(factory = component.getViewModelFactory())

    val model by updater.model.collectAsState()

    LaunchedEffect(key1 = Unit) {
        updater.labelEvents.collect {
            when (it) {

                LabelEvent.AddClick -> {
                    addImageClicked()
                }

                is LabelEvent.PictureChosen -> {
                    exitScreen(it.uriString)
                }
            }
        }
    }

    when (val currentContentState = model.contentState) {
        is PicturesStore.State.ContentState.Content -> {
            ImagesScreenContent(
                paddingValues = paddingValues,
                listTypes = model.picturesTypes,
                currentClickedType = model.currentClickedType,
                currentButtonState = model.buttonState,
                listPictures = currentContentState.listPicturesUri,
                deletingList = model.deletingList,
                pictureClick = { uriString, index ->  updater.pictureClick(uriString, index) },
                pictureLongClick = { updater.pictureLongClick(it)},
                buttonClick = { updater.buttonClick()},
                typeClick = { updater.typeClick(it)}
                )
        }

        PicturesStore.State.ContentState.Error -> {}
        PicturesStore.State.ContentState.Initial -> {}
        PicturesStore.State.ContentState.Loading -> {
            CircularLoading()
        }
    }
}

//<editor-fold desc="ImagesScreenContent">
@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ImagesScreenContent(
    paddingValues: PaddingValues,
    listTypes: List<PictureType>,
    currentClickedType: PictureType,
    currentButtonState: PicturesStore.State.ButtonState,
    listPictures: List<Uri>,
    deletingList: List<Int>,
    pictureClick: (uriString: String, index: Int) -> Unit,
    pictureLongClick: (index: Int) -> Unit,
    buttonClick: () -> Unit,
    typeClick: (PictureType) -> Unit
) {

    val imageState: MutableState<AsyncImagePainter.State> = remember {
        mutableStateOf(AsyncImagePainter.State.Loading(null))
    }

    val screenWidthDp = getScreenWidthDp()
    val startEndPadding = 8.dp
    val contentLazyGridWidth = (screenWidthDp - (startEndPadding * 2)) / 3

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
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    buttonClick()
                }
            ) {
                Text(
                    text = when (currentButtonState) {
                        PicturesStore.State.ButtonState.Add -> {
                            stringResource(R.string.add_button)
                        }

                        PicturesStore.State.ButtonState.Delete -> {
                            stringResource(R.string.delete_button)
                        }
                    },
                    fontSize = 24.sp
                )
            }
        }
    ) {

        Column {
            LazyRow(
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                items(items = listTypes) { pictureType ->
                    Row(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .width(150.dp)
                            .height(40.dp)
                            .background(
                                if (currentClickedType == pictureType)
                                    Color.LightGray
                                else Color.LightGray.copy(alpha = 0.3f)
                            )
                            .clickable {
                                typeClick(pictureType)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = pictureType.type,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

//            if (isLoadingContentState) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize(),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        CircularLoading()
//                    }
//                }
//            } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 8.dp,
                        top = 16.dp,
                        end = 8.dp
                    ),
                columns = GridCells.Fixed(3)
            ) {
                itemsIndexed(items = listPictures) { index, imageUri ->
                    Box(
                        modifier = Modifier
                            .size(contentLazyGridWidth)
                            .border(
                                border = BorderStroke(1.dp, Color.Black),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .combinedClickable(
                                onLongClick = {
                                    pictureLongClick(index)
                                },
                                onClick = {
                                    pictureClick(imageUri.toString(), index)
                                }
                            )
                    ) {

                        val request = ImageRequest
                            .Builder(LocalContext.current)
                            .data(imageUri)
                            .size(coil.size.Size.ORIGINAL)
                            .build()

                        val painter = rememberAsyncImagePainter(
                            model = request,
                            onState = {
                                imageState.value = it
                            }
                        )

                        if (imageState.value is AsyncImagePainter.State.Loading) {
                            CircularLoading()
                        } else {
                            Box {
                                if (index in deletingList) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            modifier = Modifier
                                                .size(50.dp),
                                            imageVector = ImageVector.vectorResource(
                                                id = R.drawable.ic_cross
                                            ),
                                            contentDescription = null
                                        )
                                    }
                                }
                                Image(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    painter = painter,
                                    contentDescription = "content_by_uri",
                                    alpha = if (index in deletingList) 0.2f else 1f
                                )
                            }

                        }
                    }
                }
            }
        }
    }
//    }
}
//</editor-fold>








