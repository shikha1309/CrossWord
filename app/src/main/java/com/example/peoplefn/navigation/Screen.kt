package com.example.peoplefn.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object LevelSelect : Screen("level_select")
    object Game : Screen("game/{levelId}") {
        fun createRoute(levelId: Int) = "game/$levelId"
    }
}
