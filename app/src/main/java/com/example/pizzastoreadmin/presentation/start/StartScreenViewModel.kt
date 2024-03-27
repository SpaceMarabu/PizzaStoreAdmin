package com.example.pizzastore.presentation.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class StartScreenViewModel @Inject constructor(): ViewModel() {

    private val _state = MutableStateFlow<StartScreenState>(StartScreenState.Initial)
    val state = _state.asStateFlow()

    fun changeState(state: StartScreenState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }
}