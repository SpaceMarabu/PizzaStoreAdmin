package com.example.pizzastoreadmin.presentation.images.images

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.usecases.business.GetListPicturesUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.presentation.sharedstates.ShouldLeaveScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImagesScreenViewModel @Inject constructor(
    private val getListPicturesUseCase: GetListPicturesUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ImagesScreenState>(ImagesScreenState.Initial)
    val state = _state.asStateFlow()

    private val _shouldLeaveScreenState: MutableStateFlow<ShouldLeaveScreenState> =
        MutableStateFlow(ShouldLeaveScreenState.Processing)
    val shouldLeaveScreenState = _shouldLeaveScreenState.asStateFlow()

    init {
        changeScreenState(ImagesScreenState.Content)
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

    fun getListPictures() {
        viewModelScope.launch {
            val pictures = getListPicturesUseCase.getListPictures()
            pictures
        }
    }

    private fun changeScreenState(state: ImagesScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }
}