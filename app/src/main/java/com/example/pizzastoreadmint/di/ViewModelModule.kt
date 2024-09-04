package com.example.pizzastoreadmint.di

import androidx.lifecycle.ViewModel
import com.example.pizzastoreadmint.di.ViewModelKey
import com.example.pizzastoreadmint.presentation.start.StartScreenViewModel
import com.example.pizzastoreadmint.presentation.city.cities.CitiesScreenUpdater
import com.example.pizzastoreadmint.presentation.city.onecity.OneCityScreenUpdater
import com.example.pizzastoreadmint.presentation.images.images.PicturesScreenUpdater
import com.example.pizzastoreadmint.presentation.images.oneimage.OnePictureUpdater
import com.example.pizzastoreadmint.presentation.login.LoginScreenUpdater
import com.example.pizzastoreadmint.presentation.order.oneorder.OneOrderScreenUpdater
import com.example.pizzastoreadmint.presentation.order.orders.OrdersScreenUpdater
import com.example.pizzastoreadmint.presentation.product.oneproduct.OneProductScreenUpdater
import com.example.pizzastoreadmint.presentation.product.products.ProductsScreenUpdater
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @IntoMap
    @ViewModelKey(StartScreenViewModel::class)
    @Binds
    fun bindStartViewModel(updater: StartScreenViewModel): ViewModel

    @IntoMap
    @ViewModelKey(CitiesScreenUpdater::class)
    @Binds
    fun bindCitiesScreenViewModel(updater: CitiesScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(OneCityScreenUpdater::class)
    @Binds
    fun bindOneCityScreenViewModel(updater: OneCityScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(OnePictureUpdater::class)
    @Binds
    fun bindOneImageScreenViewModel(updater: OnePictureUpdater): ViewModel

    @IntoMap
    @ViewModelKey(PicturesScreenUpdater::class)
    @Binds
    fun bindImagesScreenViewModel(updater: PicturesScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(OneProductScreenUpdater::class)
    @Binds
    fun bindProductScreenViewModel(updater: OneProductScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(ProductsScreenUpdater::class)
    @Binds
    fun bindProductsScreenViewModel(updater: ProductsScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(OrdersScreenUpdater::class)
    @Binds
    fun bindOrdersScreenViewModel(updater: OrdersScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(OneOrderScreenUpdater::class)
    @Binds
    fun bindOneOrderScreenViewModel(updater: OneOrderScreenUpdater): ViewModel

    @IntoMap
    @ViewModelKey(LoginScreenUpdater::class)
    @Binds
    fun bindLoginScreenUpdater(updater: LoginScreenUpdater): ViewModel

}
