package com.example.pizzastoreadmin.presentation.images.images

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.usecases.business.DeletePicturesUseCase
import com.example.pizzastoreadmin.domain.usecases.business.GetListPicturesUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.domain.usecases.service.PostCurrentPictureTypeUseCase
import com.example.pizzastoreadmin.domain.usecases.service.SetCurrentProductImageUriUseCase
import com.example.pizzastoreadmin.presentation.sharedstates.ShouldLeaveScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImagesScreenViewModel @Inject constructor(
    private val getListPicturesUseCase: GetListPicturesUseCase,
    private val postCurrentPictureTypeUseCase: PostCurrentPictureTypeUseCase,
    private val setCurrentProductImageUriUseCase: SetCurrentProductImageUriUseCase,
    private val deleteImageUriUseCase: DeletePicturesUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
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
        changeImagesType(PictureType.PIZZA)
    }


    //<editor-fold desc="subscribeListPicturesFlow">
    private fun subscribeListPicturesFlow() {
        viewModelScope.launch {
            getListPicturesUseCase
                .getListPictures()
                .collect {
                    Log.d("TEST_IMAGE", it[0].path.toString())
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

    //<editor-fold desc="setProductImageUri">
    fun setProductImageUri(imageUri: Uri) {
        viewModelScope.launch {
            setCurrentProductImageUriUseCase.setUri(imageUri)
        }
    }
    //</editor-fold>

    fun deleteImages(listUri: List<Uri>) {
        deleteImageUriUseCase.deletePictures(listUri)
    }

    private fun changeScreenState(state: ImagesScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }

    //<editor-fold desc="changeImagesType">
    fun changeImagesType(type: PictureType) {
        viewModelScope.launch {
            _isLoadingContent.emit(true)
            postCurrentPictureTypeUseCase.postType(type)
        }
    }
    //</editor-fold>
}