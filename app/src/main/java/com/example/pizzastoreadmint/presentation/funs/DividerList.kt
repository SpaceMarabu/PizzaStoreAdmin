package com.example.pizzastoreadmint.presentation.funs

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DividerList() {
    HorizontalDivider(
        modifier = Modifier
            .padding(start = 8.dp, top = 8.dp),
        thickness = 1.dp,
        color = Color.Gray
    )
}