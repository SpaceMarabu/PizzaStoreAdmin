package com.example.pizzastoreadmin.presentation.funs

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors().copy(
    unfocusedLabelColor = Color.LightGray,
    focusedLabelColor = Color.Black,
    cursorColor = Color.Gray
)