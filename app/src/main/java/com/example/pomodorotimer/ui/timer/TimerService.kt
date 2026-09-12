package com.example.pomodorotimer.ui.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.pomodorotimer.MainActivity
import com.example.pomodorotimer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.content.pm.ServiceInfo

import java.util.Locale

class TimerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "timer_channel"
        private const val NOTIFICATION_ID = 1

        private val _uiState = MutableStateFlow(TimerUiState())
        val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

        const val ACTION_START = "START"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_RESET = "RESET"
    }


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESET -> resetTimer()
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun startTimer() {
        if (_uiState.value.isRunning) return
        _uiState.value = _uiState.value.copy(isRunning = true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            // Starts the "Foreground" status with a notification
            startForeground(NOTIFICATION_ID, createNotification())
        }
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && _uiState.value.isRunning && _uiState.value.timeLeftInSeconds > 0) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(
                    timeLeftInSeconds = _uiState.value.timeLeftInSeconds - 1
                )
                updateNotification()
            }
            if (_uiState.value.timeLeftInSeconds <= 0) {
                _uiState.value = _uiState.value.copy(isRunning = false)
                updateNotification("Timer Finished!")
                stopForeground(STOP_FOREGROUND_DETACH)
            }
        }
    }


    private fun pauseTimer() {
        _uiState.value = _uiState.value.copy(isRunning = false)
        timerJob?.cancel()
        updateNotification()
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private fun resetTimer() {
        _uiState.value = _uiState.value.copy(
            timeLeftInSeconds = _uiState.value.totalTimeInSeconds,
            isRunning = false
        )
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(contentText: String? = null): Notification {
        val state = _uiState.value
        val minutes = state.timeLeftInSeconds / 60
        val seconds = state.timeLeftInSeconds % 60
        val timeText = contentText ?: String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro Timer")
            .setContentText(timeText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(state.isRunning)
            .setSilent(true)

        // 1. ADD PAUSE / RESUME BUTTON
        if (state.isRunning) {
            val pauseIntent = Intent(this, TimerService::class.java).apply { action = ACTION_PAUSE }
            val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(0, "PAUSE", pausePendingIntent)
        } else {
            val resumeIntent = Intent(this, TimerService::class.java).apply { action = ACTION_START }
            val resumePendingIntent = PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(0, "RESUME", resumePendingIntent)
        }

        // 2. ADD RESET BUTTON
        val resetIntent = Intent(this, TimerService::class.java).apply { action = ACTION_RESET }
        val resetPendingIntent = PendingIntent.getService(this, 3, resetIntent, PendingIntent.FLAG_IMMUTABLE)
        builder.addAction(0, "RESET", resetPendingIntent)

        return builder.build()
    }


private fun updateNotification(text: String? = null) {
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(NOTIFICATION_ID, createNotification(text))
}
private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(CHANNEL_ID, "Timer", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
}