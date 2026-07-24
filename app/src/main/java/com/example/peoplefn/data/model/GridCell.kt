package com.example.peoplefn.data.model

data class GridCell(
    val row: Int,                    // Row position on the screen
    val col: Int,                    // Column position on the screen
    val char: Char,                  // The character (e.g., 'C')
    val isRevealed: Boolean = false, // True if the user guessed the word (reveals character)
    val belongsToWordIds: List<String>, // Words intersecting at this cell (allows overlaps)
    val isJustRevealed: Boolean = false // Flag used to trigger pop/fade-in animations
)
