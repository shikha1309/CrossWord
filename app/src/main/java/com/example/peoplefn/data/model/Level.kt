package com.example.peoplefn.data.model

data class Level(
    val id: Int,                     // Level number (e.g., 1-10)
    val gridWidth: Int,              // Width of the crossword grid
    val gridHeight: Int,             // Height of the crossword grid
    val wheelLetters: List<String>,  // Letters to put inside the circle wheel
    val words: List<Word>,           // Target grid words
    val bonusWords: List<String>     // Dictionary of valid words outside the grid
)
