package com.example.pomodorotimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodorotimer.theme.*

@Composable
fun RetroBorder(
    modifier: Modifier = Modifier,
    tileSize: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val tileColors = listOf(
        RetroRed,
        RetroYellow,
        RetroBlue,
        RetroGreenButton
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RetroDarkBg)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bezel label
            Text(
                text = "ARC  ·  TIMER  ·  001",
                color = RetroGray.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontFamily = PressStart2PFontFamily,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Inner Frame with colorful block border
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()

                        val tilePx = tileSize.toPx()
                        val w = size.width
                        val h = size.height

                        var colorIdx = 0

                        // Top edge (left to right)
                        var x = 0f
                        while (x < w) {
                            val tileWidth = minOf(tilePx, w - x)
                            drawRect(
                                color = tileColors[colorIdx % tileColors.size],
                                topLeft = Offset(x, 0f),
                                size = Size(tileWidth, tilePx)
                            )
                            x += tilePx
                            colorIdx++
                        }

                        // Right edge (top to bottom)
                        var y = tilePx
                        while (y < h) {
                            val tileHeight = minOf(tilePx, h - y)
                            drawRect(
                                color = tileColors[colorIdx % tileColors.size],
                                topLeft = Offset(w - tilePx, y),
                                size = Size(tilePx, tileHeight)
                            )
                            y += tilePx
                            colorIdx++
                        }

                        // Bottom edge (right to left)
                        x = w - tilePx * 2
                        while (x >= 0f) {
                            drawRect(
                                color = tileColors[colorIdx % tileColors.size],
                                topLeft = Offset(x, h - tilePx),
                                size = Size(tilePx, tilePx)
                            )
                            x -= tilePx
                            colorIdx++
                        }

                        // Left edge (bottom to top)
                        y = h - tilePx * 2
                        while (y >= tilePx) {
                            drawRect(
                                color = tileColors[colorIdx % tileColors.size],
                                topLeft = Offset(0f, y),
                                size = Size(tilePx, tilePx)
                            )
                            y -= tilePx
                            colorIdx++
                        }
                    }
                    .padding(tileSize)
                    .background(RetroScreenBg)
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    content()
                }
            }

            // Bottom bezel label
            Text(
                text = "POWER  ●  5V",
                color = RetroGray.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontFamily = PressStart2PFontFamily,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
