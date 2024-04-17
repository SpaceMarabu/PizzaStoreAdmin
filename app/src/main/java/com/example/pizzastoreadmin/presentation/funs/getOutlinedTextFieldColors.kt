package com.example.pizzastoreadmin.presentation.funs

import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getOutlinedTextFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    unfocusedBorderColor = Color.LightGray,
    unfocusedLabelColor = Color.LightGray,
    backgroundColor = Color.White,
    textColor = Color.Black,
    focusedBorderColor = Color.Black,
    focusedLabelColor = Color.Black,
    cursorColor = Color.Gray
)