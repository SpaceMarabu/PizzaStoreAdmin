package com.example.pizzastoreadmin.di

import androidx.lifecycle.ViewModel
import com.example.pizzastore.di.ViewModelKey
import com.example.pizzastore.presentation.start.StartScreenViewModel
import com.example.pizzastoreadmin.presentation.city.cities.CitiesScreenUDFVM
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityScreenViewModel
import com.example.pizzastoreadmin.presentation.images.images.ImagesScreenViewModel
import com.example.pizzastoreadmin.presentation.images.oneimage.OneImageScreenViewModel
import com.example.pizzastoreadmin.presentation.order.oneorder.OneOrderScreenUDFVM
import com.example.pizzastoreadmin.presentation.order.orders.OrdersScreenUDFVM
import com.example.pizzastoreadmin.presentation.product.oneproduct.OneProductScreenViewModel
import com.example.pizzastoreadmin.presentation.product.products.ProductsScreenViewModel
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
    @ViewModelKey(CitiesScreenUDFVM::class)
    @Binds
    fun bindCitiesScreenViewModel(viewModel: CitiesScreenUDFVM): ViewModel

    @IntoMap
    @ViewModelKey(OneCityScreenViewModel::class)
    @Binds
    fun bindOneCityScreenViewModel(viewModel: OneCityScreenViewModel): ViewModel

    @IntoMap
    @ViewModelKey(OneImageScreenViewModel::class)
    @Binds
    fun bindOneImageScreenViewModel(viewModel: OneImageScreenViewModel): ViewModel

    @IntoMap
    @ViewModelKey(ImagesScreenViewModel::class)
    @Binds
    fun bindImagesScreenViewModel(viewModel: ImagesScreenViewModel): ViewModel

    @IntoMap
    @ViewModelKey(OneProductScreenViewModel::class)
    @Binds
    fun bindProductScreenViewModel(viewModel: OneProductScreenViewModel): ViewModel

    @IntoMap
    @ViewModelKey(ProductsScreenViewModel::class)
    @Binds
    fun bindProductsScreenViewModel(viewModel: ProductsScreenViewModel): ViewModel

    @IntoMap
    @ViewModelKey(OrdersScreenUDFVM::class)
    @Binds
    fun bindOrdersScreenViewModel(viewModel: OrdersScreenUDFVM): ViewModel

    @IntoMap
    @ViewModelKey(OneOrderScreenUDFVM::class)
    @Binds
    fun bindOneOrderScreenViewModel(viewModel: OneOrderScreenUDFVM): ViewModel

}
