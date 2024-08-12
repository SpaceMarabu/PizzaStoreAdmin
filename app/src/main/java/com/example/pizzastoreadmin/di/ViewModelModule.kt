package com.example.pizzastoreadmin.di

import androidx.lifecycle.ViewModel
import com.example.pizzastore.di.ViewModelKey
import com.example.pizzastore.presentation.start.StartScreenViewModel
import com.example.pizzastoreadmin.presentation.city.cities.CitiesScreenUpdater
import com.example.pizzastoreadmin.presentation.city.onecity.OneCityScreenUpdater
import com.example.pizzastoreadmin.presentation.images.images.ImagesScreenUpdater
import com.example.pizzastoreadmin.presentation.images.oneimage.OneImageScreenViewModel
import com.example.pizzastoreadmin.presentation.order.oneorder.OneOrderScreenUpdater
import com.example.pizzastoreadmin.presentation.order.orders.OrdersScreenUpdater
import com.example.pizzastoreadmin.presentation.product.oneproduct.OneProductScreenUpdater
import com.example.pizzastoreadmin.presentation.product.products.ProductsScreenUpdater
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
    @ViewModelKey(CitiesScreenUpdater::class)
    @Binds
    fun bindCitiesScreenViewModel(viewModel: CitiesScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(OneCityScreenUpdater::class)
    @Binds
    fun bindOneCityScreenViewModel(viewModel: OneCityScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(OneImageScreenViewModel::class)
    @Binds
    fun bindOneImageScreenViewModel(viewModel: OneImageScreenViewModel): ViewModel

    @IntoMap
    @ViewModelKey(ImagesScreenUpdater::class)
    @Binds
    fun bindImagesScreenViewModel(viewModel: ImagesScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(OneProductScreenUpdater::class)
    @Binds
    fun bindProductScreenViewModel(viewModel: OneProductScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(ProductsScreenUpdater::class)
    @Binds
    fun bindProductsScreenViewModel(viewModel: ProductsScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(OrdersScreenUpdater::class)
    @Binds
    fun bindOrdersScreenViewModel(viewModel: OrdersScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(OneOrderScreenUpdater::class)
    @Binds
    fun bindOneOrderScreenViewModel(viewModel: OneOrderScreenUpdater): ViewModel

}
