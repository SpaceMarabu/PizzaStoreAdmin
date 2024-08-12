package com.example.pizzastoreadmin.presentation.images.images

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.pizzastoreadmin.domain.entity.PictureType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImagesScreenUpdater @Inject constructor(
    storeFactory: PicturesStoreFactory
) : ViewModel() {

    val store = storeFactory.create()

    private val _labelEvents = MutableSharedFlow<LabelEvent>()
    val labelEvents = _labelEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            store.labels.collect {
                when (it) {
                    PicturesStore.Label.AddClick -> {
                        _labelEvents.emit(LabelEvent.AddClick)
                    }

                    is PicturesStore.Label.PictureChosen -> {
                        _labelEvents.emit(LabelEvent.PictureChosen(it.uriString))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val model = store.stateFlow

    fun typeClick(type: PictureType) = store.accept(PicturesStore.Intent.TypeClick(type))

    fun pictureClick(pictureUriString: String, index: Int) = store.accept(
        PicturesStore.Intent.PictureClick(
            uriString = pictureUriString,
            index = index
        )
    )

    fun pictureLongClick(index: Int) = store.accept(PicturesStore.Intent.PictureLongClick(index))

    fun buttonClick() = store.accept(PicturesStore.Intent.ButtonClick)

}