package com.example.pizzastoreadmin.presentation.city.onecity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.pizzastoreadmin.domain.entity.Point
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OneCityScreenUpdater @Inject constructor(
    storeFactory: OneCityStoreFactory
) : ViewModel() {

   val store = storeFactory.create()

    override fun onCleared() {
        super.onCleared()
        Log.d("TEST_CLEAR", "done")
    }

    private val _labelEvents = MutableSharedFlow<LabelEvents>()
    val labelEvents = _labelEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            store.labels.collect {
                when(it) {
                    OneCityStore.Label.ErrorRepositoryResponse -> {
                        _labelEvents.emit(LabelEvents.ErrorRepositoryResponse)
                    }
                    OneCityStore.Label.Exit -> {
                        _labelEvents.emit(LabelEvents.Exit)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val model = store.stateFlow

    fun changeCityName(cityName: String) =
        store.accept(OneCityStore.Intent.ChangeCityName(cityName))

    fun changePoint(point: Point) = store.accept(OneCityStore.Intent.ChangePoint(point))

    fun addPointClick() = store.accept(OneCityStore.Intent.AddPointClick)

    fun doneClick() = store.accept(OneCityStore.Intent.DoneClick)

    fun removePointClick(point: Point) = store.accept(OneCityStore.Intent.RemovePointClick(point))

}