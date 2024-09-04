package com.example.pizzastoreadmint.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pizzastoreadmint.presentation.home.MainScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<MainScreenState>(MainScreenState.Initial)
    val state = _state.asStateFlow()

    init {
        initScreen()
    }


    private fun initScreen() {
        viewModelScope.launch {
            _state.emit(MainScreenState.Content)
        }
    }
}