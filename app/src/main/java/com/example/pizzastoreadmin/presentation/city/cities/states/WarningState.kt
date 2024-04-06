package com.example.pizzastoreadmin.presentation.city.cities.states

sealed class WarningState {

    object Nothing : WarningState()

    data class DeleteComplete(
        val description: String = "Удалено успешно"
    ) : WarningState()

    data class DeleteIncomplete(
        val description: String = "Что то пошло не так"
    ) : WarningState()
}
