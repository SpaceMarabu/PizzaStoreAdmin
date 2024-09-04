package com.example.pizzastoreadmint.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColorScheme(
    primary = Black900,
    secondary = Black900,
    onPrimary = Color.White,
    onSecondary = Black500
)

@SuppressLint("ConflictingOnColor")
private val LightColorPalette = lightColorScheme(
    primary = Color.White,
    secondary = Color.Gray,
    tertiary = Color.Black,
    onPrimary = Black900,
    onSecondary = Black500,
    onTertiary = Color.White
)

@Composable
fun PizzaStoreAdminTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {

    MaterialTheme(
        colorScheme = LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}