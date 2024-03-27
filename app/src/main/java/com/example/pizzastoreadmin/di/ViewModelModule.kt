package com.example.pizzastoreadmin.di

import androidx.lifecycle.ViewModel
import com.example.pizzastore.di.ViewModelKey
import com.example.pizzastoreadmin.presentation.city.CityScreenViewModel
import com.example.pizzastore.presentation.start.StartScreenViewModel
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
    @ViewModelKey(CityScreenViewModel::class)
    @Binds
    fun bindCityScreenViewModel(viewModel: CityScreenViewModel): ViewModel

}
