package com.example.pomodorotimer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath

@Composable
fun TomatoCanvas(
    progress: Float, // 0.0f (empty) to 1.0f (full)
    modifier: Modifier = Modifier
){
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 1. Calculate the liquid surface Y-coordinate
        val fillY = height * (1f - progress)

        // 2. Define the tomato outline shape
        val tomatoPath = Path().apply {
            addOval(Rect(left = 0f, top = height * 0.15f, right = width, bottom = height))
        }

        // 3. Clip everything inside our tomato stencil
        clipPath(tomatoPath) {
            // Base background color (empty state)
            drawPath(path = tomatoPath, color = Color(0xFFFFEBEE))

            // Liquid fill drawn from fillY down to the bottom
            drawRect(
                color = Color(0xFFE53935), // Tomato Red
                topLeft = Offset(x = 0f, y = fillY),
                size = Size(width = width, height = height - fillY)
            )
        }
    }
}