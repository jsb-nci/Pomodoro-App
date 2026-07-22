package com.example.pomodorotimer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
@HiltViewModel
class TimerViewModel @Inject constructor(): ViewModel()  {
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    fun startTimer() {
        // Avoid launching multiple timer loops if already running
        if (_uiState.value.isRunning) return

        _uiState.value = _uiState.value.copy(isRunning = true)

        viewModelScope.launch {
            while (_uiState.value.isRunning && _uiState.value.timeLeftInSeconds > 0) {
                delay(1000L)

                _uiState.value = _uiState.value.copy(
                    timeLeftInSeconds = _uiState.value.timeLeftInSeconds - 1
                )
            }

            // Stop running once timer reaches 0
            if (_uiState.value.timeLeftInSeconds == 0) {
                _uiState.value = _uiState.value.copy(isRunning = false)
            }
        }
    }
    fun pauseTimer(){
        if(_uiState.value.isRunning == false )return
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        _uiState.value = _uiState.value.copy(
            timeLeftInSeconds = _uiState.value.totalTimeInSeconds,
            isRunning = false
        )
    }
}

