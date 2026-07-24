package com.example.peoplefn.utils

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GestureUtils {
    /**
     * Calculates positions of N items distributed evenly around a circle center (cx, cy) with radius R.
     */
    fun calculateWheelPositions(
        letters: List<String>,
        center: Offset,
        radius: Float
    ): List<Offset> {
        val count = letters.size
        if (count == 0) return emptyList()

        val angleStep = 2 * Math.PI / count
        // Start from top (-PI/2)
        val startAngle = -Math.PI / 2

        return letters.indices.map { i ->
            val angle = startAngle + i * angleStep
            val x = (center.x + radius * cos(angle)).toFloat()
            val y = (center.y + radius * sin(angle)).toFloat()
            Offset(x, y)
        }
    }

    /**
     * Checks if a point is within hitRadius of a target position.
     */
    fun isPointNearNode(point: Offset, nodePos: Offset, hitRadius: Float): Boolean {
        val dx = point.x - nodePos.x
        val dy = point.y - nodePos.y
        val distance = sqrt(dx * dx + dy * dy)
        return distance <= hitRadius
    }
}
