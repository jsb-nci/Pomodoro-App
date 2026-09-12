package com.example.pomodorotimer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pomodorotimer.theme.*

private val TOMATO_GRID = arrayOf(
    "......GGGG......",
    ".....GGGGGG.....",
    "....GGGGGGGG....",
    ".....GGGGGG.....",
    "....RRRRRRRR....",
    "...RRRRRRRRRR...",
    "..RRRRRRRRRRRR..",
    ".RRWWWRRRRRRRRR.",
    ".RRWWWRRRRRRRRR.",
    "RRRRRRRRRRRRRRRR",
    "RRRRRRRRRRRRRRRR",
    "RRRRRRRRRRRRRRRR",
    ".RRRRRRRRRRRRRR.",
    ".RRRRRRRRRRRRRR.",
    "..RRRRRRRRRRRR..",
    "...RRRRRRRRRR...",
    "....RRRRRRRR....",
    ".....RRRRRR....."
)

@Composable
fun PixelTomato(
    progress: Float, // 0.0f (full time remaining) to 1.0f (no time remaining)
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    // fillFraction: 1.0 = 100% time left (full red), 0.0 = 0% time left (drained)
    val fillFraction = (1f - progress).coerceIn(0f, 1f)

    Canvas(modifier = modifier.size(size)) {
        val numRows = TOMATO_GRID.size
        val numCols = TOMATO_GRID[0].length

        val pixelWidth = this.size.width / numCols
        val pixelHeight = this.size.height / numRows

        // Body rows are indices 4 to 17 (14 total body rows)
        val bodyStartRow = 4
        val bodyEndRow = numRows - 1
        val totalBodyRows = bodyEndRow - bodyStartRow + 1

        // Number of body rows (from bottom up) that remain filled with red
        val filledBodyRowsCount = (totalBodyRows * fillFraction).toInt()
        val fillCutoffRow = bodyEndRow - filledBodyRowsCount

        for (r in 0 until numRows) {
            val rowStr = TOMATO_GRID[r]
            val isBodyRow = r >= bodyStartRow

            for (c in 0 until numCols) {
                val char = rowStr[c]
                if (char == '.') continue

                val color = when (char) {
                    'G' -> RetroStemLeaf
                    'R', 'W' -> {
                        if (isBodyRow && r <= fillCutoffRow) {
                            // Drained/Empty portion of the tomato body
                            RetroRedDark
                        } else {
                            if (char == 'W') RetroHighlight else RetroRed
                        }
                    }
                    else -> Color.Transparent
                }

                if (color != Color.Transparent) {
                    val topLeft = Offset(c * pixelWidth, r * pixelHeight)
                    val pixelSize = Size(pixelWidth, pixelHeight)

                    drawRect(
                        color = color,
                        topLeft = topLeft,
                        size = pixelSize
                    )

                    // Draw subtle dark line at top of pixel for 8-bit texture/grid effect
                    drawRect(
                        color = Color.Black.copy(alpha = 0.2f),
                        topLeft = topLeft,
                        size = Size(pixelWidth, 1f)
                    )
                }
            }
        }
    }
}
