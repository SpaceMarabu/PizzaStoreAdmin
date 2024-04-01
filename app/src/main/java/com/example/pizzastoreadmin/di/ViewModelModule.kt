package com.example.pizzastoreadmin.di

import androidx.lifecycle.ViewModel
import com.example.pizzastore.di.ViewModelKey
import com.example.pizzastore.presentation.start.StartScreenViewModel
import com.example.pizzastoreadmin.presentation.city.cities.CitiesScreenViewModel
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityScreenViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @IntoMap
    @ViewModelKey(StartScreenViewModel::class)
    @Binds
    fun bindStartViewModel(viewModel: StartScreenViewModel): ViewModel

    @IntoMap
    @ViewModelKey(CitiesScreenViewModel::class)
    @Binds
    fun bindCitiesScreenViewModel(viewModel: CitiesScreenViewModel): ViewModel

    @IntoMap
    @ViewModelKey(OneCityScreenViewModel::class)
    @Binds
    fun bindOneCityScreenViewModel(viewModel: OneCityScreenViewModel): ViewModel

}
