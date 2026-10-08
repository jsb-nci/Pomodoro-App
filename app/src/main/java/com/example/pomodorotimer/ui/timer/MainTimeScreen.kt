package com.example.pomodorotimer.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pomodorotimer.theme.*
import com.example.pomodorotimer.ui.components.PixelTomato
import com.example.pomodorotimer.ui.components.RetroBorder
import com.example.pomodorotimer.ui.components.RetroButton

@Composable
fun MainTimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    MainTimerContent(
        uiState = uiState,
        onStartClick = { viewModel.startTimer() },
        onPauseClick = { viewModel.pauseTimer() },
        onResetClick = { viewModel.resetTimer() }
    )
}

@Composable
fun MainTimerContent(
    uiState: TimerUiState,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = uiState.timeLeftInSeconds / 60
    val seconds = uiState.timeLeftInSeconds % 60
    val timeText = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    RetroBorder(modifier = modifier) {
        // 1. Header: ★ POMODORO ★
        Text(
            text = "★  POMODORO  ★",
            color = RetroYellow,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PressStart2PFontFamily,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        // 2. Pixel Tomato (Drains as time runs down)
        PixelTomato(
            progress = uiState.progress,
            size = 180.dp
        )

        // 3. Digital Clock Text (Glowing CRT Green with Pixel Font)
        val glowColor = when (uiState.phase) {
            TimerPhase.WORK -> RetroGreen
            TimerPhase.BREAK -> RetroGreen
            TimerPhase.BIG_BREAK -> RetroBlue
            TimerPhase.FINISHED -> RetroYellow
        }

        Text(
            text = timeText,
            style = TextStyle(
                color = glowColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PressStart2PFontFamily,
                letterSpacing = 2.sp,
                shadow = Shadow(
                    color = glowColor.copy(alpha = 0.8f),
                    offset = Offset(0f, 0f),
                    blurRadius = 16f
                )
            )
        )

        // 4. Status Indicator
        val statusText = when (uiState.phase) {
            TimerPhase.WORK -> {
                val base = "SESSION ${uiState.currentSession}/8"
                if (!uiState.isRunning && uiState.timeLeftInSeconds < uiState.totalTimeInSeconds) {
                    ">> $base (PAUSED) <<"
                } else {
                    ">> $base <<"
                }
            }
            TimerPhase.BREAK -> ">> QUICK BREAK <<"
            TimerPhase.BIG_BREAK -> ">> BIG BREAK <<"
            TimerPhase.FINISHED -> ">> CYCLE COMPLETE <<"
        }

        val statusColor = when (uiState.phase) {
            TimerPhase.WORK -> RetroOrange
            TimerPhase.BREAK -> RetroGreen
            TimerPhase.BIG_BREAK -> RetroBlue
            TimerPhase.FINISHED -> RetroYellow
        }

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PressStart2PFontFamily,
            letterSpacing = 1.sp
        )

        // 5. Progress Bar (Exactly 8 blocks for 8 sessions)
        val totalBlocks = 8
        val completedSessions = when (uiState.phase) {
            TimerPhase.FINISHED -> 8
            TimerPhase.WORK -> uiState.currentSession - 1
            else -> uiState.currentSession // BREAK or BIG_BREAK means session just finished
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalBlocks) {
                val isCurrentSession = (i == completedSessions && uiState.phase == TimerPhase.WORK)
                
                val blockColor = when {
                    i < completedSessions -> RetroGreen // Finished sessions
                    isCurrentSession -> RetroYellow      // Session in progress
                    else -> RetroGreenDark             // Future sessions
                }

                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 20.dp)
                        .background(blockColor)
                )
            }
        }

        // 6. Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            if (!uiState.isRunning) {
                val buttonText = if (uiState.phase == TimerPhase.FINISHED) "RESTART" else "START"
                RetroButton(
                    text = buttonText,
                    onClick = onStartClick,
                    containerColor = RetroGreenButton,
                    contentColor = Color.Black,
                    modifier = Modifier.weight(1f)
                )
            } else {
                RetroButton(
                    text = "PAUSE",
                    onClick = onPauseClick,
                    containerColor = RetroOrange,
                    contentColor = Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }

            RetroButton(
                text = "RESET",
                onClick = onResetClick,
                containerColor = RetroRed,
                contentColor = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        // 7. Session Footer
        val footerText = when (uiState.phase) {
            TimerPhase.WORK -> "FOCUS TIME · 25 MIN"
            TimerPhase.BREAK -> "REST TIME · 5 MIN"
            TimerPhase.BIG_BREAK -> "REST TIME · 60 MIN"
            TimerPhase.FINISHED -> "TOTAL PROGRESS · 8/8"
        }
        Text(
            text = footerText,
            color = RetroGray,
            fontSize = 8.sp,
            fontFamily = PressStart2PFontFamily,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainTimerPreview() {
    PomodoroTimerTheme {
        MainTimerContent(
            uiState = TimerUiState(
                timeLeftInSeconds = 1500,
                totalTimeInSeconds = 1500,
                isRunning = false,
                currentSession = 1,
                phase = TimerPhase.WORK
            ),
            onStartClick = {},
            onPauseClick = {},
            onResetClick = {}
        )
    }
}


