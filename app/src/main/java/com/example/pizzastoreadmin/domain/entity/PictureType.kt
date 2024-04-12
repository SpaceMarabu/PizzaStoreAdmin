package com.example.pizzastoreadmin.domain.entity

sealed class PictureType(
    val type: String
) {

    object PIZZA : PictureType(TYPE_PIZZA)
    object ROLL : PictureType(TYPE_ROLL)
    object STARTER : PictureType(TYPE_STARTER)
    object DESSERT : PictureType(TYPE_DESSERT)
    object DRINK : PictureType(TYPE_DRINK)

    object STORY : PictureType(TYPE_STORY)

    companion object {
        private const val TYPE_PIZZA = "pizza"
        private const val TYPE_ROLL = "roll"
        private const val TYPE_STARTER = "starter"
        private const val TYPE_DESSERT = "dessert"
        private const val TYPE_DRINK = "drink"
        private const val TYPE_STORY = "story"
    }
}