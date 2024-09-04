package com.example.pizzastoreadmint.presentation.funs

import android.util.DisplayMetrics
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun getScreenWidthDp(): Dp {
    val displayMetrics: DisplayMetrics = LocalContext.current.resources.displayMetrics
    return (displayMetrics.widthPixels / displayMetrics.density).dp
}