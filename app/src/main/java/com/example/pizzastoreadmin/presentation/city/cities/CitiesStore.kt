package com.example.pizzastoreadmin.presentation.city.cities

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.pizzastoreadmin.data.repository.states.DBResponse
import com.example.pizzastoreadmin.domain.entity.City
import com.example.pizzastoreadmin.domain.usecases.business.DeleteCityUseCase
import com.example.pizzastoreadmin.domain.usecases.business.GetAllCitiesUseCase
import com.example.pizzastoreadmin.domain.usecases.service.GetDbResponseUseCase
import com.example.pizzastoreadmin.domain.usecases.service.SetCurrentCityUseCase
import com.example.pizzastoreadmin.presentation.city.cities.CitiesStore.Intent
import com.example.pizzastoreadmin.presentation.city.cities.CitiesStore.Label
import com.example.pizzastoreadmin.presentation.city.cities.CitiesStore.State
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

interface CitiesStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class SelectCity(val city: City) : Intent

        data class UnselectCity(val city: City) : Intent

        data class EditCity(val city: City) : Intent

        data object ButtonClick : Intent
    }

    data class State(
        val contentState: ContentState,
        val buttonState: ButtonState = ButtonState.Add,
        val selectedCities: List<City> = listOf()
    ) {

        sealed interface ButtonState {

            data object Add : ButtonState

            data object Delete : ButtonState
        }

        sealed interface ContentState {

            data object Initial : ContentState

            data object Loading : ContentState

            data object Error : ContentState

            data class Content(val cities: List<City>) : ContentState
        }
    }

    sealed interface Label {

        data object AddCity : Label

        data class EditCity(val city: City) : Label

        data object DeleteComplete : Label

        data object DeleteFailed : Label
    }
}

class CitiesStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getCitiesUseCase: GetAllCitiesUseCase,
    private val deleteCityUseCase: DeleteCityUseCase,
    private val setCurrentCityUseCase: SetCurrentCityUseCase,
    private val getDbResponseUseCase: GetDbResponseUseCase
) {

    fun create(): CitiesStore =
        object : CitiesStore, Store<Intent, State, Label> by storeFactory.create(
            name = "CitiesStore",
            initialState = State(
                contentState = State.ContentState.Initial
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data object LoadingCities : Action

        data object ErrorCitiesLoading : Action

        data class CitiesLoaded(val cities: List<City>) : Action

        data object DeleteComplete : Action

        data object DeleteFailed : Action

    }

    private sealed interface Msg {

        data object LoadingCities : Msg

        data object ErrorCitiesLoading : Msg

        data class CitiesLoaded(val cities: List<City>) : Msg

        data class SelectCity(val city: City) : Msg

        data class UnselectCity(val city: City) : Msg

        data object DeleteCities : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {

            scope.launch {
                getDbResponseUseCase.getDbResponseFlow().collect {
                    when (it) {
                        DBResponse.Complete -> {
                            dispatch(Action.DeleteComplete)
                        }

                        is DBResponse.Error -> {
                            dispatch(Action.DeleteFailed)
                        }

                        DBResponse.Processing -> {}
                    }
                }
            }

            try {
                scope.launch {
                    dispatch(Action.LoadingCities)
                    getCitiesUseCase
                        .getCitiesFlow()
                        .filter { it.isNotEmpty() }
                        .collect {
                            dispatch(Action.CitiesLoaded(it))
                        }
                }
            } catch (e: Exception) {
                dispatch(Action.ErrorCitiesLoading)
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {

            when (intent) {
                is Intent.SelectCity -> {
                    dispatch(Msg.SelectCity(intent.city))
                }

                is Intent.EditCity -> {
                    val currentCity = intent.city
                    setCurrentCityUseCase.setCity(currentCity)
                    publish(Label.EditCity(currentCity))
                }

                is Intent.UnselectCity -> {
                    dispatch(Msg.UnselectCity(intent.city))
                }

                Intent.ButtonClick -> {
                    val currentButtonState = getState().buttonState
                    when (currentButtonState) {
                        State.ButtonState.Add -> {
                            publish(Label.AddCity)
                        }

                        State.ButtonState.Delete -> {
                            val currentSelectedCities = getState().selectedCities
                            deleteCityUseCase.deleteCity(currentSelectedCities)
                            dispatch(Msg.DeleteCities)
                        }
                    }
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {

            when (action) {
                is Action.CitiesLoaded -> {
                    dispatch(Msg.CitiesLoaded(action.cities))
                }

                Action.DeleteComplete -> {
                    publish(Label.DeleteComplete)
                }

                Action.DeleteFailed -> {
                    publish(Label.DeleteFailed)
                }

                Action.ErrorCitiesLoading -> {
                    dispatch(Msg.ErrorCitiesLoading)
                }

                Action.LoadingCities -> {
                    dispatch(Msg.LoadingCities)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.CitiesLoaded -> {
                    this.copy(
                        contentState = State.ContentState.Content(msg.cities)
                    )
                }

                Msg.ErrorCitiesLoading -> {
                    this.copy(
                        contentState = State.ContentState.Error
                    )
                }

                Msg.LoadingCities -> {
                    this.copy(
                        contentState = State.ContentState.Loading
                    )
                }

                is Msg.SelectCity -> {

                    val currentSelectedCities = this.selectedCities.toMutableList()
                    currentSelectedCities.add(msg.city)
                    this.copy(
                        selectedCities = currentSelectedCities.toList(),
                        buttonState = getButtonState(currentSelectedCities)
                    )
                }

                is Msg.UnselectCity -> {
                    val currentSelectedCities = this.selectedCities.toMutableList()
                    currentSelectedCities.remove(msg.city)
                    this.copy(
                        selectedCities = currentSelectedCities.toList(),
                        buttonState = getButtonState(currentSelectedCities)
                    )
                }

                Msg.DeleteCities -> {
                    val selectedCities = listOf<City>()
                    this.copy(
                        selectedCities = selectedCities,
                        buttonState = getButtonState(selectedCities)
                    )
                }
            }

        private fun getButtonState(selectedCitiesList: List<City>): State.ButtonState {
            return if (selectedCitiesList.isEmpty()) {
                State.ButtonState.Add
            } else {
                State.ButtonState.Delete
            }
        }
    }

}
