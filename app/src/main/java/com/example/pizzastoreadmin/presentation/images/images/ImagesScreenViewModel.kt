package com.example.pizzastoreadmin.presentation.images.images

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.usecases.business.GetListPicturesUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.domain.usecases.service.PostCurrentPictureTypeUseCase
import com.example.pizzastoreadmin.presentation.sharedstates.ShouldLeaveScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImagesScreenViewModel @Inject constructor(
    private val getListPicturesUseCase: GetListPicturesUseCase,
    private val postCurrentPictureTypeUseCase: PostCurrentPictureTypeUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ImagesScreenState>(ImagesScreenState.Initial)
    val state = _state.asStateFlow()

    private val _shouldLeaveScreenState: MutableStateFlow<ShouldLeaveScreenState> =
        MutableStateFlow(ShouldLeaveScreenState.Processing)
    val shouldLeaveScreenState = _shouldLeaveScreenState.asStateFlow()

    private val _listPicturesUri:

    init {
//        changeImagesType(PictureType.PIZZA)
        subscribeDbResponse()
    }

    //<editor-fold desc="subscribeDbResponse">
    private fun subscribeDbResponse() {
        viewModelScope.launch {
            getDbResponseUseCase
                .getDbResponseFlow()
                .collect {

                    when (it) {
                        DBResponse.Complete -> {
                            _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Exit)
                        }

                        is DBResponse.Error -> {
                            _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Error(it.description))
                        }

                        DBResponse.Processing -> {
                            _shouldLeaveScreenState.emit(ShouldLeaveScreenState.Processing)
                        }
                    }

                }
        }
    }
    //</editor-fold>

    private fun getListPictures(type: PictureType): List<Uri> {
        var listUri = listOf<Uri>()
        viewModelScope.launch {
            getListPicturesUseCase.getListPictures(type.type)
                .collect {
                    listUri = it
                }
        }
        return listUri
    }

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

    private fun changeScreenState(state: ImagesScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }

    fun changeImagesType(type: PictureType) {
        val listPicturesUri = getListPictures(type)
        viewModelScope.launch {
            changeScreenState(
                ImagesScreenState
                    .Content(listPicturesUri = listPicturesUri)
            )
        }
    }
}