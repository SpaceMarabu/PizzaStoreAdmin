package com.example.pizzastoreadmin.presentation.images.images

import android.net.Uri
import com.example.pizzastoreadmin.domain.entity.PictureType

sealed class ImagesScreenState {

    object Initial : ImagesScreenState()

    object Loading : ImagesScreenState()

    object Content : ImagesScreenState()
}
