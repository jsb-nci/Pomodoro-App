package com.example.pomodorotimer.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pomodorotimer.ui.components.TomatoCanvas

@Composable
fun MainTimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {

    //  Subscribe to StateFlow updates
    val uiState by viewModel.uiState.collectAsState()

    val minutes = uiState.timeLeftInSeconds / 60
    val seconds = uiState.timeLeftInSeconds % 60
    val timeText = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
// 🍅 Canvas
        TomatoCanvas(
            progress = uiState.progress,
            modifier = Modifier.size(260.dp)
        )

        // ⏱️ Timer Text
        Text(
            text = timeText,
            fontSize = 54.sp,
            style = MaterialTheme.typography.displayLarge
        )

        // 🔘 Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!uiState.isRunning) {
                Button(onClick = { viewModel.startTimer() }) {
                    Text("Start")
                }
            } else {
                Button(onClick = { viewModel.pauseTimer() }) {
                    Text("Pause")
                }
            }

            OutlinedButton(onClick = { viewModel.resetTimer() }) {
                Text("Reset")
            }
        }
    }
}
