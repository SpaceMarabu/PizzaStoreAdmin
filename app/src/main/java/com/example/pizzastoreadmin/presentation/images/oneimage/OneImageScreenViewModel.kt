package com.example.pizzastoreadmin.presentation.images.oneimage

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.domain.usecases.business.PutImageToStorageUseCase
import com.example.pizzastoreadmin.presentation.sharedstates.ShouldLeaveScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OneImageScreenViewModel @Inject constructor(
    private val putImageToStorageUseCase: PutImageToStorageUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<OneImageScreenState>(OneImageScreenState.Initial)
    val state = _state.asStateFlow()

    private val _shouldLeaveScreenState: MutableStateFlow<ShouldLeaveScreenState> =
        MutableStateFlow(ShouldLeaveScreenState.Processing)
    val shouldLeaveScreenState = _shouldLeaveScreenState.asStateFlow()

    init {
        changeScreenState(OneImageScreenState.Content)
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

    fun getAllPictureTypes() = listOf(
        PictureType.PIZZA,
        PictureType.ROLL,
        PictureType.STARTER,
        PictureType.DESSERT,
        PictureType.DRINK,
        PictureType.STORY
    )

    private fun changeScreenState(state: OneImageScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }

    fun putImageToStorage(name: String, type: String, imageByteArray: ByteArray, ) {
        putImageToStorageUseCase.putImage(name, type, imageByteArray)
    }

    fun readBytes(context: Context, uri: Uri): ByteArray? =
        context.contentResolver.openInputStream(uri)?.use {
            it.buffered().readBytes()
        }
}