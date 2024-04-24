package com.example.pizzastoreadmin.presentation.funs.dropdown

import com.example.pizzastoreadmin.domain.entity.ObjectWithType

data class DropDownMenuStates(
    val isProductMenuExpanded: Boolean,
    val selectedOption: ObjectWithType
)
