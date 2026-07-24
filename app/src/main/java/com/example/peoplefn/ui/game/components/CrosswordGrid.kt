package com.example.peoplefn.ui.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peoplefn.data.model.GridCell

@Composable
fun CrosswordGrid(
    gridWidth: Int,
    gridHeight: Int,
    gridMatrix: List<List<GridCell?>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (r in 0 until gridHeight) {
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (c in 0 until gridWidth) {
                    val cell = if (r < gridMatrix.size && c < gridMatrix[r].size) {
                        gridMatrix[r][c]
                    } else null

                    GridCellTile(cell = cell)
                }
            }
        }
    }
}

@Composable
private fun GridCellTile(cell: GridCell?) {
    if (cell == null) {
        // Empty space in crossword layout
        Box(
            modifier = Modifier
                .size(60.dp)
                .padding(2.dp)
        )
        return
    }

    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(cell.isJustRevealed) {
        if (cell.isJustRevealed) {
            scaleAnim.animateTo(
                targetValue = 1.25f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
            )
        }
    }

    val isRevealed = cell.isRevealed

    Box(
        modifier = Modifier
            .size(60.dp)
            .padding(2.dp)
            .scale(scaleAnim.value)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isRevealed) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF176), Color(0xFFFBC02D))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xBBFFFFFF), Color(0x77FFFFFF))
                    )
                }
            )
            .border(
                width = 1.5.dp,
                color = if (isRevealed) Color(0xFFF57F17) else Color(0x66FFFFFF),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isRevealed) {
            Text(
                text = cell.char.uppercase(),
                color = Color(0xFF37474F),
                fontWeight = FontWeight.Black,
                fontSize = 28.sp
            )
        }
    }
}

