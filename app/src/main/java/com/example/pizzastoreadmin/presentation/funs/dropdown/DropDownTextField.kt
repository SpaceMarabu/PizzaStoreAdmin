package com.example.pizzastoreadmin.presentation.funs.dropdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
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
import com.example.pizzastoreadmin.presentation.funs.getOutlinedTextFieldColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownTextField(
    dropDownMenuStates: DropDownMenuStates,
    options: List<ObjectWithType>,
    onOptionOrOutsideClicked: (DropDownMenuStates) -> Unit
) {


    ExposedDropdownMenuBox(
        expanded = dropDownMenuStates.isProductMenuExpanded,
        onExpandedChange = {
            val currentStates = dropDownMenuStates.copy(isProductMenuExpanded = true)
            onOptionOrOutsideClicked(currentStates)
        }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp
                ),

            shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp)),
            readOnly = true,
            value = dropDownMenuStates.selectedOption.type,
            onValueChange = { },
            label = { Text("Type") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = dropDownMenuStates.isProductMenuExpanded
                )
            },
            colors = getOutlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = dropDownMenuStates.isProductMenuExpanded,
            onDismissRequest = {
                val currentStates = dropDownMenuStates.copy(isProductMenuExpanded = false)
                onOptionOrOutsideClicked(currentStates)
            }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        val currentStates = dropDownMenuStates.copy(
                            isProductMenuExpanded = false,
                            selectedOption = selectionOption
                        )
                        onOptionOrOutsideClicked(currentStates)
                    },
                    text = {
                        Text(text = selectionOption.type)

                    }
                )
            }
        }
    }
}







