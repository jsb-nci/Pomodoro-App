package com.example.pomodorotimer.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Bouncing Tomato for Page 1
@Composable
fun AnimatedBouncingTomato() {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y_offset"
    )

    Box(modifier = Modifier.size(120.dp).offset(y = offsetY.dp), contentAlignment = Alignment.Center) {
        // You can reuse your existing PixelTomato here!
        PixelTomato(progress = 1f, size = 100.dp)
    }
}

// 2. Blinking Screen for Page 3 (Stay Focused)
@Composable
fun AnimatedBlinkingMonitor() {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
        // A simple text representation, or you can draw a custom canvas here
        Text(
            text = "[_]",
            fontSize = 64.sp,
            modifier = Modifier.alpha(alpha),
            color = com.example.pomodorotimer.theme.RetroGreen
        )
    }
}
