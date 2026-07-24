package com.example.peoplefn.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peoplefn.ui.game.components.CrosswordGrid
import com.example.peoplefn.ui.game.components.LetterWheel
import com.example.peoplefn.ui.game.components.TopBar
import com.example.peoplefn.ui.game.components.PauseDialog
import com.example.peoplefn.ui.game.components.GifBackground

@Composable
fun GameScreen(
    levelId: Int,
    onBackToHome: () -> Unit,
    onNextLevel: (Int) -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(levelId) {
        viewModel.loadLevel(levelId)
    }

    val currentOnNextLevel by rememberUpdatedState(onNextLevel)

    LaunchedEffect(uiState.isLevelComplete) {
        if (uiState.isLevelComplete) {
            val currentLevel = uiState.level
            if (currentLevel != null) {
                kotlinx.coroutines.delay(2000)
                currentOnNextLevel(currentLevel.id + 1)
            }
        }
    }

    val level = uiState.level ?: return

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Game GIF Background (plays for 2 seconds, then freezes)
        GifBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // 1. Top Bar
            TopBar(
                levelId = level.id,
                coins = uiState.coins,
                onBackClick = onBackToHome,
                onPauseClick = { viewModel.togglePause() },
                onHintClick = { viewModel.useHint() }
            )

            // 2. Crossword Grid Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CrosswordGrid(
                    gridWidth = level.gridWidth,
                    gridHeight = level.gridHeight,
                    gridMatrix = uiState.gridMatrix
                )
            }

            // 3. Word Preview Bar & Feedback Popup Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                // Animation states for preview chip
                val animScale = remember { Animatable(1f) }
                val animAlpha = remember { Animatable(1f) }
                val shakeOffsetX = remember { Animatable(0f) }

                LaunchedEffect(uiState.feedbackType) {
                    when (uiState.feedbackType) {
                        FeedbackType.VALID_GRID, FeedbackType.BONUS -> {
                            animScale.animateTo(1.3f, animationSpec = tween(300))
                            animAlpha.animateTo(0f, animationSpec = tween(200))
                        }
                        FeedbackType.INVALID -> {
                            for (i in 0..2) {
                                shakeOffsetX.animateTo(-15f, animationSpec = tween(50))
                                shakeOffsetX.animateTo(15f, animationSpec = tween(50))
                            }
                            shakeOffsetX.animateTo(0f, animationSpec = tween(50))
                        }
                        else -> {
                            animScale.snapTo(1f)
                            animAlpha.snapTo(1f)
                            shakeOffsetX.snapTo(0f)
                        }
                    }
                }

                val chipBgColor = when (uiState.feedbackType) {
                    FeedbackType.VALID_GRID -> Color(0xFF4CAF50)
                    FeedbackType.BONUS -> Color(0xFFAB47BC)
                    FeedbackType.INVALID -> Color(0xFFE53935)
                    else -> Color(0xFFFFD54F)
                }

                val chipTextColor = when (uiState.feedbackType) {
                    FeedbackType.VALID_GRID, FeedbackType.BONUS, FeedbackType.INVALID -> Color.White
                    else -> Color(0xFF37474F)
                }

                // Live Word Formation Chip
                if (uiState.currentFormedWord.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(shakeOffsetX.value.roundToInt(), 0) }
                            .graphicsLayer {
                                scaleX = animScale.value
                                scaleY = animScale.value
                                alpha = animAlpha.value
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipBgColor)
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = uiState.currentFormedWord,
                            color = chipTextColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }

                // Visual Feedback Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.feedbackMessage != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    val (bgColor, textColor) = when (uiState.feedbackType) {
                        FeedbackType.VALID_GRID -> Color(0xFF4CAF50) to Color.White
                        FeedbackType.BONUS -> Color(0xFFAB47BC) to Color.White
                        FeedbackType.ALREADY_FOUND -> Color(0xFFFF9800) to Color.White
                        FeedbackType.INVALID -> Color(0xFFE53935) to Color.White
                        FeedbackType.NONE -> Color.Gray to Color.White
                    }

                    uiState.feedbackMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgColor)
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = msg,
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. Circular Letter Wheel Component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                LetterWheel(
                    letters = uiState.wheelLetters,
                    onWordSelected = { word -> viewModel.submitWord(word) },
                    onSelectionChange = { current -> viewModel.updateCurrentFormedWord(current) },
                    onShuffleClick = { viewModel.shuffleWheel() },
                    feedbackType = uiState.feedbackType,
                    isLockInteraction = uiState.isLockInteraction
                )
            }
        }

        // 5. Pause Modal Dialog
        if (uiState.isPaused) {
            PauseDialog(
                onResume = { viewModel.togglePause() },
                onMainMenu = {
                    viewModel.togglePause()
                    onBackToHome()
                },
                onDismissRequest = { viewModel.togglePause() }
            )
        }

        // 6. Level Complete Victory Overlay (avoids freeze-like screen)
        AnimatedVisibility(
            visible = uiState.isLevelComplete,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)), // Dim background
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Victory Trophy",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "LEVEL CLEARED!",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Loading next level...",
                        color = Color(0xB3FFFFFF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

