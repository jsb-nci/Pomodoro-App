package com.example.pomodorotimer.ui.timer

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.copy
import kotlin.compareTo

data class TimerUiState(
    val timeLeftInSeconds: Int = 25 * 60, // 25 minutes default
    val totalTimeInSeconds: Int = 25 * 60,
    val isRunning: Boolean = false
) {
    // 🧮 Automatically calculates the progress for TomatoCanvas (0.0f to 1.0f)
    val progress: Float
        get() = if (totalTimeInSeconds > 0) {
            1f - (timeLeftInSeconds.toFloat() / totalTimeInSeconds.toFloat())
        } else 0f
}
// Keep the TimerUiState definition if it's not in a separate file
@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
): ViewModel()  {

    // Observe the state directly from the Service's companion object
    val uiState: StateFlow<TimerUiState> = TimerService.uiState


    fun startTimer() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    fun pauseTimer() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_PAUSE
        }
        context.startService(intent)
    }
    fun resetTimer() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_RESET
        }
        context.startService(intent)
    }
}

