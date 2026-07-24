package com.example.peoplefn.data.model

enum class WordDirection {
    HORIZONTAL,
    VERTICAL
}

data class Word(
    val id: String,
    val text: String,
    val startRow: Int,
    val startCol: Int,
    val direction: WordDirection,
    val isSolved: Boolean = false
)
