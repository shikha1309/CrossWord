package com.example.peoplefn.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.peoplefn.ui.game.GameScreen
import com.example.peoplefn.ui.home.HomeScreen
import com.example.peoplefn.ui.level.LevelScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400))
            }
        ) {
            HomeScreen(
                onPlayClick = { levelId ->
                    navController.navigate(Screen.Game.createRoute(levelId))
                },
                onLevelSelectClick = {
                    navController.navigate(Screen.LevelSelect.route)
                }
            )
        }

        composable(
            route = Screen.LevelSelect.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400))
            }
        ) {
            LevelScreen(
                onLevelSelected = { levelId ->
                    navController.navigate(Screen.Game.createRoute(levelId)) {
                        popUpTo(Screen.LevelSelect.route) { inclusive = false }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("levelId") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            ),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(400))
            }
        ) { backStackEntry ->
            val context = androidx.compose.ui.platform.LocalContext.current
            val repository = androidx.compose.runtime.remember { com.example.peoplefn.data.repository.GameRepository(context) }
            val totalLevels = androidx.compose.runtime.remember { repository.getAllLevels().size }
            val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1


            GameScreen(
                levelId = levelId,
                onBackToHome = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onNextLevel = { nextId ->
                    if (nextId <= totalLevels) {
                        navController.navigate(Screen.Game.createRoute(nextId)) {
                            popUpTo(Screen.Game.route) { inclusive = true }
                        }
                    } else {
                        // Completed last level, go back to home screen
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                }
            )

        }
    }
}
