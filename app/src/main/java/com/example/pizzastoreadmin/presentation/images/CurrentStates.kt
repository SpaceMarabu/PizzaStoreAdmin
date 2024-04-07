package com.example.pizzastoreadmin.presentation.images

import android.net.Uri
import com.example.pizzastoreadmin.domain.entity.City

data class CurrentStates(
    var isErrorInTextField: Boolean,
    var isTextNotEmpty: Boolean,
    var pictureName: String,
    var imageUri: Uri?
)
