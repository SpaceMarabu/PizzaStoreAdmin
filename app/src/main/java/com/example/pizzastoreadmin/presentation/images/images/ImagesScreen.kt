package com.example.pizzastoreadmin.presentation.images.images

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
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val viewModel: ImagesScreenViewModel = viewModel(factory = component.getViewModelFactory())

    val screenState by viewModel.state.collectAsState()

    when (screenState) {

        ImagesScreenState.Initial -> {}

        is ImagesScreenState.Content -> {
            ImagesScreenContent(
                viewModel = viewModel,
                paddingValues = paddingValues,
                addImageClicked = {
                    addImageClicked()
                }
            ) { uriString ->
                exitScreen(uriString)
            }
        }

        ImagesScreenState.Loading -> {
            CircularLoading()
        }
    }
}

//<editor-fold desc="ImagesScreenContent">
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImagesScreenContent(
    viewModel: ImagesScreenViewModel,
    paddingValues: PaddingValues,
    addImageClicked: () -> Unit,
    exitScreen: (uriString: String) -> Unit
) {

    val listTypes = viewModel.getAllPictureTypes()
    val screenWidthDp = getScreenWidthDp()
    val startEndPadding = 8.dp
    val contentLazyGridWidth = (screenWidthDp - (startEndPadding * 2)) / 3

    val listPictures by viewModel.listPicturesUriState.collectAsState()
    val isLoadingContentState by viewModel.isLoadingContent.collectAsState()

    val listImagesToDelete: MutableState<List<Int>> = remember {
        mutableStateOf(listOf())
    }
    val currentListToDelete = listImagesToDelete.value

    var clickedType: PictureType by remember {
        mutableStateOf(PictureType.PIZZA)
    }

    val imageState: MutableState<AsyncImagePainter.State> = remember {
        mutableStateOf(AsyncImagePainter.State.Loading(null))
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
                    if (currentListToDelete.isEmpty()) {
                        addImageClicked()
                    } else {
                        val listUriToDelete = mutableListOf<Uri>()
                        currentListToDelete.forEach {
                            listUriToDelete.add(listPictures[it])
                        }
                        viewModel.deleteImages(listUriToDelete)
                        listImagesToDelete.value = listOf()
                    }
                }
            ) {
                Text(
                    text = if (currentListToDelete.isEmpty()) {
                        stringResource(R.string.add_button)
                    } else {
                        stringResource(R.string.delete_button)
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
                                if (clickedType == pictureType)
                                    Color.LightGray
                                else Color.LightGray.copy(alpha = 0.3f)
                            )
                            .clickable {
                                clickedType = pictureType
                                viewModel.changeImagesType(pictureType)
                                listImagesToDelete.value = listOf()
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

            if (isLoadingContentState) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularLoading()
                    }
                }
            } else {
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
                                        if (index in currentListToDelete) {
                                            listImagesToDelete.value =
                                                currentListToDelete - index
                                        } else {
                                            listImagesToDelete.value =
                                                currentListToDelete + index
                                        }
                                    },
                                    onClick = {
                                        if (currentListToDelete.isNotEmpty()) {
                                            if (index in currentListToDelete) {
                                                listImagesToDelete.value =
                                                    currentListToDelete - index
                                            } else {
                                                listImagesToDelete.value =
                                                    currentListToDelete + index
                                            }
                                        } else {
                                            exitScreen(imageUri.toString())
                                        }
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
                                    if (index in currentListToDelete) {
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
                                        alpha = if (index in currentListToDelete) 0.2f else 1f
                                    )
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}
//</editor-fold>








