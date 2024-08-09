package com.example.pizzastoreadmin.presentation.funs.dropdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pizzastoreadmin.domain.entity.ObjectWithType
import com.example.pizzastoreadmin.presentation.funs.getOutlinedColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownTextFieldNew(
    selectedOption: ObjectWithType,
    isDropDownExpanded: Boolean,
    options: List<ObjectWithType>,
    onOptionClicked: (ObjectWithType) -> Unit,
    onDropDownClicked: () -> Unit,
    onScreenClicked: () -> Unit
) {

    ExposedDropdownMenuBox(
        modifier = Modifier.padding(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp),
        expanded = isDropDownExpanded,
        onExpandedChange = {
            onDropDownClicked()
        }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp)),
            readOnly = true,
            value = selectedOption.type,
            onValueChange = { },
            label = { Text("Type") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = isDropDownExpanded
                )
            },
            colors = getOutlinedColors()
        )
        DropdownMenu(
            modifier = Modifier
                .exposedDropdownSize(true),
            expanded = isDropDownExpanded,
            onDismissRequest = {
                onScreenClicked()
            }
        ) {
            options.filter { it != selectedOption }.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        onOptionClicked(selectionOption)
                    },
                    text = {
                        Text(text = selectionOption.type)
                    }
                )
            }
        }
    }
}







