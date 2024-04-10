package com.example.pizzastoreadmin.presentation.images

import android.net.Uri

data class CurrentScreenContentStates(
    var isTextNotEmpty: Boolean,
    var pictureName: String,
    var imageUri: Uri?
)
