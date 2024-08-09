package com.example.pizzastoreadmin.presentation.city.onecity

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.pizzastore.domain.entity.Point
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.usecases.business.AddOrEditCItyUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetCurrentCityUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityStore.Intent
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityStore.Label
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface OneCityStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class ChangeCityName(val name: String) : Intent

        data class ChangePoint(val point: Point) : Intent

        data object AddPointClick : Intent

        data class RemovePointClick(val point: Point) : Intent

        data object DoneClick : Intent
    }

    data class State(
        val contentState: ContentState = ContentState.Initial,
        val textFieldValuesValid: TextFieldValuesValid = TextFieldValuesValid()
    ) {

        class TextFieldValuesValid(
            val cityNameCorrect: Boolean = true,
            val pointsValuesCorrect: List<Boolean> = listOf()
        )

        sealed interface ContentState {

            data object Initial : ContentState

            data object Loading : ContentState

            data object Error : ContentState

            data class Content(val city: City) : ContentState
        }
    }

    sealed interface Label {

        data object ErrorRepositoryResponse : Label

        data object Exit : Label
    }
}

class OneCityStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val addOrEditCityUseCase: AddOrEditCItyUseCase,
    private val getCurrentCityUseCase: GetCurrentCityUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) {

    fun create(): OneCityStore =
        object : OneCityStore, Store<Intent, State, Label> by storeFactory.create(
            name = "OneCityStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data object LoadingCity : Action

        data object ErrorLoadingCity : Action

        data object ErrorRepositoryResponse : Action

        data object SuccessRepositoryResponse : Action

        data class LoadedContent(val city: City) : Action
    }

    private sealed interface Msg {

        data object LoadingCity : Msg

        data object ErrorLoadingCity : Msg

        data class LoadedContent(val city: City) : Msg

        data class ChangeCityName(val name: String) : Msg

        data class ChangePoint(val point: Point) : Msg

        data object AddPointClick : Msg

        data class RemovePointClick(val point: Point) : Msg

        data class ChangeFieldsValidation(val validation: State.TextFieldValuesValid) : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            try {
                scope.launch {
                    dispatch(Action.LoadingCity)
                    getCurrentCityUseCase.getCity()
                        .collect {
                            dispatch(Action.LoadedContent(it))
                        }
                }
            } catch (e: Exception) {
                dispatch(Action.ErrorLoadingCity)
            }

            try {
                scope.launch {
                    getDbResponseUseCase.getDbResponseFlow().collect {
                        when (it) {
                            DBResponse.Complete -> {
                                dispatch(Action.SuccessRepositoryResponse)
                            }

                            is DBResponse.Error -> {
                                dispatch(Action.ErrorRepositoryResponse)
                            }

                            DBResponse.Processing -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                dispatch(Action.ErrorRepositoryResponse)
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.AddPointClick -> {
                    dispatch(Msg.AddPointClick)
                }

                is Intent.ChangeCityName -> {
                    dispatch(Msg.ChangeCityName(intent.name))
                }

                is Intent.ChangePoint -> {
                    dispatch(Msg.ChangePoint(intent.point))
                }

                Intent.DoneClick -> {
                    val currentContentState = getState().contentState
                    if (currentContentState is State.ContentState.Content) {
                        val currentPointsValidationState = checkTextFields(currentContentState.city)
                        if (
                            currentPointsValidationState.cityNameCorrect
                            && currentPointsValidationState.pointsValuesCorrect.all { it }
                        ) {
                            addOrEditCityUseCase.addOrEditCity(currentContentState.city)
                        } else {
                            dispatch(Msg.ChangeFieldsValidation(currentPointsValidationState))
                        }
                    }
                }

                is Intent.RemovePointClick -> {
                    dispatch(Msg.RemovePointClick(intent.point))
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {

            when (action) {
                Action.ErrorLoadingCity -> {
                    dispatch(Msg.ErrorLoadingCity)
                }

                Action.ErrorRepositoryResponse -> {
                    publish(Label.ErrorRepositoryResponse)
                }

                is Action.LoadedContent -> {
                    dispatch(Msg.LoadedContent(action.city))
                }

                Action.LoadingCity -> {
                    dispatch(Msg.LoadingCity)
                }

                Action.SuccessRepositoryResponse -> {
                    publish(Label.Exit)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                Msg.AddPointClick -> {
                    val currentState = this.contentState
                    if (currentState is State.ContentState.Content) {
                        val newListPoints = addNewPointToList(currentState.city.points)
                        val newCity = currentState.city.copy(points = newListPoints)
                        val validTextFields = initialValidTextFields(newCity.points.size)
                        this.copy(
                            contentState = State.ContentState.Content(newCity),
                            textFieldValuesValid = validTextFields
                        )
                    } else {
                        this
                    }
                }

                is Msg.ChangeCityName -> {
                    val currentContentState = this.contentState
                    if (currentContentState is State.ContentState.Content) {
                        val newCity = currentContentState.city.copy(name = msg.name)
                        this.copy(contentState = State.ContentState.Content(newCity))
                    } else {
                        this
                    }
                }

                is Msg.ChangePoint -> {
                    val currentContentState = this.contentState
                    if (currentContentState is State.ContentState.Content) {
                        val newPoints = currentContentState.city.points.map {
                            if (it.id == msg.point.id) msg.point else it
                        }
                        val newCity = currentContentState.city.copy(points = newPoints)
                        val newContentState = currentContentState.copy(newCity)
                        this.copy(contentState = newContentState)
                    } else {
                        this
                    }
                }

                Msg.ErrorLoadingCity -> {
                    this.copy(contentState = State.ContentState.Error)
                }

                is Msg.LoadedContent -> {
                    val validFields = initialValidTextFields(msg.city.points.size)
                    this.copy(
                        contentState = State.ContentState.Content(msg.city),
                        textFieldValuesValid = validFields
                    )
                }

                Msg.LoadingCity -> {
                    this.copy(contentState = State.ContentState.Loading)
                }

                is Msg.RemovePointClick -> {
                    val currentContentState = this.contentState
                    if (currentContentState is State.ContentState.Content) {
                        val points = currentContentState.city.points.toMutableList()
                        points.remove(msg.point)
                        val newCity =
                            currentContentState.city.copy(points = points.updatePointIds())
                        val validTextFields = initialValidTextFields(newCity.points.size)
                        val newContentState = currentContentState.copy(newCity)
                        this.copy(
                            contentState = newContentState,
                            textFieldValuesValid = validTextFields
                        )
                    } else {
                        this
                    }
                }

                is Msg.ChangeFieldsValidation -> {
                    this.copy(textFieldValuesValid = msg.validation)
                }
            }

    }

    companion object {

        private fun List<Point>.updatePointIds(): List<Point> {
            return this.mapIndexed { index, point -> point.copy(id = index + 1) }
        }

        private fun addNewPointToList(points: List<Point>): List<Point> {

            var maxId = 0

            points.forEach { point ->
                if (point.id > maxId) maxId = point.id
            }

            return points + Point(
                id = maxId + 1
            )
        }

        private fun checkTextFields(
            city: City
        ): State.TextFieldValuesValid {
            val regex = Regex("\\d+\\.\\d+\\,\\ ?\\d+\\.\\d+")
            val ciyNameIsCorrect = city.name.isNotBlank()
            val pointsCorrect = city.points.map {
                it.coords.isNotBlank()
                        && it.coords.trim().matches(regex)
            }
            return State.TextFieldValuesValid(ciyNameIsCorrect, pointsCorrect)
        }

        private fun initialValidTextFields(
            count: Int
        ): State.TextFieldValuesValid {
            val initialListValidation: MutableList<Boolean> = mutableListOf()
            repeat(count) {
                initialListValidation.add(true)
            }
            return State.TextFieldValuesValid(
                cityNameCorrect = true,
                pointsValuesCorrect = initialListValidation
            )
        }
    }
}









