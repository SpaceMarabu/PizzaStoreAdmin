package com.example.pizzastoreadmin.presentation.images.images

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.usecases.business.DeletePicturesUseCase
import com.example.pizzastoreadmin.domain.usecases.business.GetListPicturesUseCase
import com.example.pizzastoreadmin.domain.usecases.service.PostCurrentPictureTypeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImagesScreenViewModel @Inject constructor(
    private val getListPicturesUseCase: GetListPicturesUseCase,
    private val postCurrentPictureTypeUseCase: PostCurrentPictureTypeUseCase,
    private val deleteImageUriUseCase: DeletePicturesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ImagesScreenState>(ImagesScreenState.Initial)
    val state = _state.asStateFlow()

    private val _listPicturesUriState = MutableStateFlow<List<Uri>>(listOf())
    val listPicturesUriState = _listPicturesUriState.asStateFlow()

    private val _isLoadingContent = MutableStateFlow(true)
    val isLoadingContent = _isLoadingContent.asStateFlow()

    init {
        changeScreenState(ImagesScreenState.Content)
        subscribeListPicturesFlow()
        changeImagesType()
    }

    //<editor-fold desc="subscribeListPicturesFlow">
    private fun subscribeListPicturesFlow() {
        viewModelScope.launch {
            getListPicturesUseCase
                .getListPictures()
                .collect {
                    _isLoadingContent.emit(false)
                    _listPicturesUriState.emit(it)
                }
        }
    }
    //</editor-fold>

    //<editor-fold desc="getAllPictureTypes">
    fun getAllPictureTypes() = listOf(
        PictureType.PIZZA,
        PictureType.ROLL,
        PictureType.STARTER,
        PictureType.DESSERT,
        PictureType.DRINK,
        PictureType.STORY
    )
    //</editor-fold>

    //<editor-fold desc="deleteImages">
    fun deleteImages(listUri: List<Uri>) {
        viewModelScope.launch {
            _isLoadingContent.emit(true)
        }
        deleteImageUriUseCase.deletePictures(listUri)
    }
    //</editor-fold>

    //<editor-fold desc="changeScreenState">
    private fun changeScreenState(state: ImagesScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }
    //</editor-fold>

    //<editor-fold desc="changeImagesType">
    fun changeImagesType(type: PictureType = PictureType.PIZZA) {
        viewModelScope.launch {
            _isLoadingContent.emit(true)
            postCurrentPictureTypeUseCase.postType(type)
        }
    }
    //</editor-fold>
}