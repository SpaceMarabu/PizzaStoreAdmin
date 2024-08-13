package com.example.pizzastoreadmin.presentation.images.images

import android.net.Uri
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.pizzastoreadmin.domain.entity.PictureType
import com.example.pizzastoreadmin.domain.usecases.business.DeletePicturesUseCase
import com.example.pizzastoreadmin.domain.usecases.business.GetListPicturesUseCase
import com.example.pizzastoreadmin.domain.usecases.service.PostCurrentPictureTypeUseCase
import com.example.pizzastoreadmin.presentation.images.getAllPictureTypes
import com.example.pizzastoreadmin.presentation.images.getButtonState
import com.example.pizzastoreadmin.presentation.images.images.PicturesStore.Intent
import com.example.pizzastoreadmin.presentation.images.images.PicturesStore.Label
import com.example.pizzastoreadmin.presentation.images.images.PicturesStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface PicturesStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class TypeClick(val type: PictureType) : Intent

        data class PictureClick(val uriString: String, val index: Int) : Intent

        data class PictureLongClick(val index: Int) : Intent

        data object ButtonClick : Intent
    }

    data class State(
        val contentState: ContentState,
        val buttonState: ButtonState,
        val deletingList: List<Int>,
        val picturesTypes: List<PictureType>,
        val currentClickedType: PictureType,
        val deletingListNotEmpty: Boolean
    ) {

        sealed interface ButtonState {

            data object Add : ButtonState

            data object Delete : ButtonState
        }

        sealed interface ContentState {

            data object Initial : ContentState

            data object Loading : ContentState

            data object Error : ContentState

            data class Content(val listPicturesUri: List<Uri>) : ContentState
        }
    }

    sealed interface Label {

        data object AddClick : Label

        data class PictureChosen(val uriString: String) : Label
    }
}

class PicturesStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getListPicturesUseCase: GetListPicturesUseCase,
    private val postCurrentPictureTypeUseCase: PostCurrentPictureTypeUseCase,
    private val deleteImageUriUseCase: DeletePicturesUseCase
) {

    fun create(): PicturesStore =
        object : PicturesStore, Store<Intent, State, Label> by storeFactory.create(
            name = "PicturesStore",
            initialState = State(
                contentState = State.ContentState.Initial,
                buttonState = State.ButtonState.Add,
                deletingList = listOf(),
                picturesTypes = getAllPictureTypes(),
                currentClickedType = PictureType.PIZZA,
                deletingListNotEmpty = false
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data object Loading : Action

        data object LoadingError : Action

        data class ContentLoaded(val listPicturesUri: List<Uri>) : Action
    }

    private sealed interface Msg {

        data class TypeClick(val type: PictureType) : Msg

        data class PictureClick(val uriString: String, val index: Int) : Msg

        data class PictureLongClick(val index: Int) : Msg

        data object Loading : Msg

        data object LoadingError : Msg

        data class ContentLoaded(val listPicturesUri: List<Uri>) : Msg

        data object DeletingFinished: Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {

            try {
                scope.launch {
                    dispatch(Action.Loading)
                    getListPicturesUseCase
                        .getListPictures()
                        .collect {
                            dispatch(Action.ContentLoaded(it))
                        }
                }
            } catch (e: Exception) {
                dispatch(Action.LoadingError)
            }

            scope.launch {
                postCurrentPictureTypeUseCase.postType(PictureType.PIZZA)
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.ButtonClick -> {
                    val currentState = getState()
                    when (currentState.buttonState) {
                        State.ButtonState.Add -> {
                            publish(Label.AddClick)
                        }

                        State.ButtonState.Delete -> {
                            val listUriToDelete = mutableListOf<Uri>()
                            if (currentState.contentState is State.ContentState.Content) {
                                currentState.deletingList.forEach {
                                    listUriToDelete.add(currentState.contentState.listPicturesUri[it])
                                }
                                deleteImageUriUseCase.deletePictures(listUriToDelete)
                                dispatch(Msg.DeletingFinished)
                            }
                        }
                    }
                }

                is Intent.PictureClick -> {
                    val currentState = getState()
                    if (currentState.deletingListNotEmpty) {
                        dispatch(Msg.PictureClick(intent.uriString, intent.index))
                    } else {
                        publish(Label.PictureChosen(intent.uriString))
                    }
                }

                is Intent.PictureLongClick -> {
                    dispatch(Msg.PictureLongClick(intent.index))
                }

                is Intent.TypeClick -> {
                    scope.launch {
                        postCurrentPictureTypeUseCase.postType(intent.type)
                    }
                    dispatch(Msg.TypeClick(intent.type))
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                is Action.ContentLoaded -> {
                    dispatch(Msg.ContentLoaded(action.listPicturesUri))
                }

                Action.Loading -> {
                    dispatch(Msg.Loading)
                }

                Action.LoadingError -> {
                    dispatch(Msg.LoadingError)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.ContentLoaded -> {
                    this.copy(
                        contentState = State.ContentState.Content(msg.listPicturesUri)
                    )
                }

                Msg.Loading -> {
                    this.copy(contentState = State.ContentState.Loading)
                }

                Msg.LoadingError -> {
                    this.copy(contentState = State.ContentState.Error)
                }

                is Msg.PictureClick -> {
                    val currentListToDelete = this.deletingList.toMutableList()
                    if (deletingList.contains(msg.index)) {
                        currentListToDelete.remove(msg.index)
                    } else {
                        currentListToDelete.add(msg.index)
                    }
                    this.copy(
                        deletingList = currentListToDelete,
                        deletingListNotEmpty = currentListToDelete.isNotEmpty(),
                        buttonState = currentListToDelete.getButtonState()
                    )
                }

                is Msg.PictureLongClick -> {
                    val currentListToDelete = this.deletingList.toMutableList()
                    if (deletingList.contains(msg.index)) {
                        currentListToDelete.remove(msg.index)
                    } else {
                        currentListToDelete.add(msg.index)
                    }
                    this.copy(
                        deletingList = currentListToDelete,
                        deletingListNotEmpty = currentListToDelete.isNotEmpty(),
                        buttonState = currentListToDelete.getButtonState()
                    )
                }

                is Msg.TypeClick -> {
                    this.copy(currentClickedType = msg.type)
                }

                Msg.DeletingFinished -> {
                    this.copy(
                        deletingList = listOf(),
                        deletingListNotEmpty = false,
                        buttonState = listOf<Int>().getButtonState()
                    )
                }
            }
    }
}











