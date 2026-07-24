package com.example.peoplefn.data.model

data class Letter(
    val index: Int,
    val char: Char,
    val x: Float = 0f,
    val y: Float = 0f,
    val isSelected: Boolean = false,
    val angleDegrees: Double = 0.0
)
