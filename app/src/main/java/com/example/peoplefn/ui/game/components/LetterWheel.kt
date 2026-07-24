package com.example.peoplefn.ui.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.peoplefn.ui.game.FeedbackType
import com.example.peoplefn.utils.GestureUtils

@Composable
fun LetterWheel(
    letters: List<String>,
    onWordSelected: (String) -> Unit,
    onSelectionChange: (String) -> Unit,
    onShuffleClick: () -> Unit,
    modifier: Modifier = Modifier,
    feedbackType: FeedbackType = FeedbackType.NONE,
    isLockInteraction: Boolean = false,
    wheelSize: Dp = 280.dp
) {
    val density = LocalDensity.current
    val nodeRadiusPx = with(density) { 26.dp.toPx() }
    val hitRadiusPx = with(density) { 38.dp.toPx() }

    val selectedIndices = remember { mutableStateListOf<Int>() }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }

    val currentIsLockInteraction by rememberUpdatedState(isLockInteraction)
    val currentFeedbackType by rememberUpdatedState(feedbackType)
    val currentOnWordSelected by rememberUpdatedState(onWordSelected)

    val currentWord = remember(selectedIndices.toList(), letters) {
        selectedIndices.mapNotNull { idx -> letters.getOrNull(idx) }.joinToString("")
    }
    val latestCurrentWord by rememberUpdatedState(currentWord)

    LaunchedEffect(currentWord) {
        onSelectionChange(currentWord)
    }

    LaunchedEffect(isLockInteraction) {
        if (!isLockInteraction) {
            selectedIndices.clear()
            currentTouchPoint = null
        }
    }

    Box(
        modifier = modifier.size(wheelSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(letters) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            if (currentIsLockInteraction) return@detectDragGestures
                            selectedIndices.clear()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = (size.width / 2f) - nodeRadiusPx - 16.dp.toPx()
                            val nodePositions = GestureUtils.calculateWheelPositions(letters, center, radius)

                            // Find touched node
                            nodePositions.forEachIndexed { index, pos ->
                                if (GestureUtils.isPointNearNode(startOffset, pos, hitRadiusPx)) {
                                    selectedIndices.add(index)
                                }
                            }
                            currentTouchPoint = startOffset
                        },
                        onDrag = { change, _ ->
                            if (currentIsLockInteraction) return@detectDragGestures
                            change.consume()
                            val currentPos = change.position
                            currentTouchPoint = currentPos

                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = (size.width / 2f) - nodeRadiusPx - 16.dp.toPx()
                            val nodePositions = GestureUtils.calculateWheelPositions(letters, center, radius)

                            // Check collision with nodes
                            nodePositions.forEachIndexed { index, pos ->
                                if (GestureUtils.isPointNearNode(currentPos, pos, hitRadiusPx)) {
                                    if (!selectedIndices.contains(index)) {
                                        selectedIndices.add(index)
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            if (currentIsLockInteraction) return@detectDragGestures
                            if (latestCurrentWord.isNotEmpty()) {
                                currentOnWordSelected(latestCurrentWord)
                            }
                            currentTouchPoint = null
                            if (latestCurrentWord.isEmpty()) {
                                selectedIndices.clear()
                            }
                        },
                        onDragCancel = {
                            if (currentIsLockInteraction) return@detectDragGestures
                            selectedIndices.clear()
                            currentTouchPoint = null
                        }
                    )
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f
            val nodeCircleRadius = outerRadius - nodeRadiusPx - 16.dp.toPx()
            val nodePositions = GestureUtils.calculateWheelPositions(letters, center, nodeCircleRadius)

            // 1. Draw Wheel Plate Outer Ring / Shadow
            drawCircle(
                color = Color(0x33000000),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x99283593), Color(0xDD1A237E)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius - 4.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color(0x44FFFFFF),
                radius = outerRadius - 4.dp.toPx(),
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // 2. Draw Dynamic Path between selected letters & finger position
            if (selectedIndices.isNotEmpty()) {
                val path = Path()
                val firstPos = nodePositions[selectedIndices.first()]
                path.moveTo(firstPos.x, firstPos.y)

                for (i in 1 until selectedIndices.size) {
                    val nextPos = nodePositions[selectedIndices[i]]
                    path.lineTo(nextPos.x, nextPos.y)
                }

                currentTouchPoint?.let { touch ->
                    val lastNodePos = nodePositions[selectedIndices.last()]
                    path.lineTo(touch.x, touch.y)
                }

                val (pathGlowColor, pathSolidBrush) = when (currentFeedbackType) {
                    FeedbackType.VALID_GRID -> Color(0x664CAF50) to Brush.linearGradient(listOf(Color(0xFF81C784), Color(0xFF4CAF50)))
                    FeedbackType.BONUS -> Color(0x66AB47BC) to Brush.linearGradient(listOf(Color(0xFFBA68C8), Color(0xFF8E24AA)))
                    FeedbackType.INVALID -> Color(0x66E53935) to Brush.linearGradient(listOf(Color(0xFFE57373), Color(0xFFD32F2F)))
                    else -> Color(0x66FFD54F) to Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00)))
                }

                // Draw outer glowing line stroke
                drawPath(
                    path = path,
                    color = pathGlowColor,
                    style = Stroke(
                        width = 16.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw inner solid line stroke
                drawPath(
                    path = path,
                    brush = pathSolidBrush,
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 3. Draw Letter Nodes
            val textPaint = android.graphics.Paint().apply {
                textSize = 24.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            nodePositions.forEachIndexed { index, pos ->
                val isSelected = selectedIndices.contains(index)
                val nodeR = if (isSelected) nodeRadiusPx * 1.18f else nodeRadiusPx

                // Shadow / Glow behind selected node
                if (isSelected) {
                    val nodeGlowColor = when (currentFeedbackType) {
                        FeedbackType.VALID_GRID -> Color(0x884CAF50)
                        FeedbackType.BONUS -> Color(0x88AB47BC)
                        FeedbackType.INVALID -> Color(0x88E53935)
                        else -> Color(0x88FFD54F)
                    }
                    drawCircle(
                        color = nodeGlowColor,
                        radius = nodeR + 6.dp.toPx(),
                        center = pos
                    )
                }

                // Node Fill
                if (isSelected) {
                    val nodeFillBrush = when (currentFeedbackType) {
                        FeedbackType.VALID_GRID -> Brush.verticalGradient(
                            colors = listOf(Color(0xFF81C784), Color(0xFF388E3C)),
                            startY = pos.y - nodeR,
                            endY = pos.y + nodeR
                        )
                        FeedbackType.BONUS -> Brush.verticalGradient(
                            colors = listOf(Color(0xFFBA68C8), Color(0xFF7B1FA2)),
                            startY = pos.y - nodeR,
                            endY = pos.y + nodeR
                        )
                        FeedbackType.INVALID -> Brush.verticalGradient(
                            colors = listOf(Color(0xFFE57373), Color(0xFFD32F2F)),
                            startY = pos.y - nodeR,
                            endY = pos.y + nodeR
                        )
                        else -> Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFEE58), Color(0xFFF57F17)),
                            startY = pos.y - nodeR,
                            endY = pos.y + nodeR
                        )
                    }
                    drawCircle(
                        brush = nodeFillBrush,
                        radius = nodeR,
                        center = pos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = nodeR,
                        center = pos,
                        style = Stroke(width = 2.dp.toPx())
                    )
                } else {
                    drawCircle(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE0E0E0)),
                            startY = pos.y - nodeR,
                            endY = pos.y + nodeR
                        ),
                        radius = nodeR,
                        center = pos
                    )
                    drawCircle(
                        color = Color(0x33000000),
                        radius = nodeR,
                        center = pos,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Node Text
                textPaint.color = if (isSelected) Color(0xFF37474F).toArgb() else Color(0xFF1A237E).toArgb()
                val fontMetrics = textPaint.fontMetrics
                val textY = pos.y - (fontMetrics.ascent + fontMetrics.descent) / 2f
                val charStr = letters.getOrNull(index) ?: ""

                drawContext.canvas.nativeCanvas.drawText(
                    charStr,
                    pos.x,
                    textY,
                    textPaint
                )
            }
        }

        // Center Shuffle Button
        IconButton(
            onClick = onShuffleClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x44FFFFFF))
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Shuffle",
                tint = Color.White
            )
        }
    }
}
