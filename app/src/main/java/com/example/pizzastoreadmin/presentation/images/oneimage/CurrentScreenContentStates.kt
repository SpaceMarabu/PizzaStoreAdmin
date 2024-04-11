package com.example.pizzastoreadmin.presentation.images.oneimage

import android.net.Uri

data class CurrentScreenContentStates(
    var isTextNotEmpty: Boolean,
    var pictureName: String,
    var imageUri: Uri?
)
