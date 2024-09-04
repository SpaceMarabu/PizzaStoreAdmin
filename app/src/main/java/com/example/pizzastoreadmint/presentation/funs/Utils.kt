package com.example.pizzastoreadmint.presentation.funs

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.Dp

fun showToastWarn(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun Modifier.oneSidedBorder(
    borderWidth: Dp,
    leftPoint: Offset,
    rightPoint: Offset,
    color: Color
): Modifier = this.then(
    Modifier.drawBehind {
        val width = size.width
        val height = size.height
        drawLine(color = Color.Black, start = leftPoint, end = rightPoint, strokeWidth = 1f)
    }
)

@Composable
fun getOutlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
    focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
    cursorColor = MaterialTheme.colorScheme.onSecondary,
    focusedSupportingTextColor = MaterialTheme.colorScheme.onPrimary,
).copy(focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary)