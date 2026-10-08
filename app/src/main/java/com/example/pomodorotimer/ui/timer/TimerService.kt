package com.example.pomodorotimer.ui.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
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
import java.util.Locale

class TimerService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null
    
    // Tracks the absolute end time in milliseconds so the timer doesn't drift when the phone sleeps
    private var targetEndTimeMs: Long = 0L

    companion object {
        private const val WORK_TIME = 25 * 60
        private const val BREAK_TIME = 5 * 60
        private const val BIG_BREAK_TIME = 60 * 60
        private const val CHANNEL_ID = "pomodoro_timer_v2"
        private const val NOTIFICATION_ID = 1

        private val _uiState = MutableStateFlow(TimerUiState())
        val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

        const val ACTION_START = "START"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_RESET = "RESET"
    }

    private fun moveToNextPhase() {
        val currentState = _uiState.value
        val nextState = when (currentState.phase) {
            TimerPhase.WORK -> {
                if (currentState.currentSession == 4) {
                    currentState.copy(
                        phase = TimerPhase.BIG_BREAK,
                        timeLeftInSeconds = BIG_BREAK_TIME,
                        totalTimeInSeconds = BIG_BREAK_TIME,
                        isRunning = true // Starts automatically
                    )
                } else if (currentState.currentSession == 8) {
                    currentState.copy(
                        phase = TimerPhase.FINISHED,
                        timeLeftInSeconds = 0,
                        isRunning = false
                    )
                } else {
                    currentState.copy(
                        phase = TimerPhase.BREAK,
                        timeLeftInSeconds = BREAK_TIME,
                        totalTimeInSeconds = BREAK_TIME,
                        isRunning = true // Starts automatically
                    )
                }
            }

            TimerPhase.BREAK, TimerPhase.BIG_BREAK -> {
                // Break ends: Prepare next WORK session
                currentState.copy(
                    phase = TimerPhase.WORK,
                    currentSession = currentState.currentSession + 1,
                    timeLeftInSeconds = WORK_TIME,
                    totalTimeInSeconds = WORK_TIME,
                    isRunning = false
                )
            }

            TimerPhase.FINISHED -> currentState
        }

        _uiState.value = nextState
        playDingSound()
        updateNotification(isSilent = false)

        if (nextState.isRunning) {
            // Automatically transitions into the next phase
            targetEndTimeMs = SystemClock.elapsedRealtime() + (nextState.timeLeftInSeconds * 1000L)
        } else {
            // Wait for user to manually press start again
            timerJob?.cancel()
            try {
                // We use STOP_FOREGROUND_REMOVE to ensure the system fully cleans up the foreground state
                // This prevents the "button blip" bug on Android 14+ when re-starting from a detached state.
                stopForeground(STOP_FOREGROUND_REMOVE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playDingSound() {
        // Option 1: A retro 8-bit style beep sequence using Android's built-in ToneGenerator.
        // This perfectly matches your app's 8-bit theme!
        serviceScope.launch {
            try {
                val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
                // 3 short beeps, 1 long beep
                toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 150)
                delay(200)
                toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 150)
                delay(200)
                toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 150)
                delay(200)
                toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 500)
                delay(500)
                toneGen.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /* 
        // Option 2: Using a custom MP3 or WAV file.
        // To use this, create a folder at app/src/main/res/raw/
        // Place an audio file inside, like 'retro_alarm.mp3'
        // Then uncomment the code below:
        
        try {
            val mediaPlayer = android.media.MediaPlayer.create(applicationContext, R.raw.retro_alarm)
            mediaPlayer.setOnCompletionListener { it.release() }
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        */
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

    override fun onBind(p0: Intent?): IBinder? = null

    private fun startTimer() {
        if (_uiState.value.isRunning) return

        if (_uiState.value.phase == TimerPhase.FINISHED) {
            _uiState.value = TimerUiState(
                timeLeftInSeconds = WORK_TIME,
                totalTimeInSeconds = WORK_TIME,
                currentSession = 1,
                phase = TimerPhase.WORK
            )
        }
        _uiState.value = _uiState.value.copy(isRunning = true)

        try {
            val notification = createNotification(isSilent = true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Calculate absolute end time for accurate background tracking
        targetEndTimeMs = SystemClock.elapsedRealtime() + (_uiState.value.timeLeftInSeconds * 1000L)

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && _uiState.value.isRunning) {
                val remainingMs = targetEndTimeMs - SystemClock.elapsedRealtime()
                if (remainingMs > 0) {
                    val remainingSeconds = kotlin.math.ceil(remainingMs / 1000.0).toInt()
                    // Only update the state if the second has actually changed
                    if (_uiState.value.timeLeftInSeconds != remainingSeconds) {
                        _uiState.value = _uiState.value.copy(timeLeftInSeconds = remainingSeconds)
                        updateNotification(isSilent = true)
                    }
                    delay(200L) // Delay less than a second to keep the UI snappy
                } else {
                    _uiState.value = _uiState.value.copy(timeLeftInSeconds = 0)
                     moveToNextPhase()
                }
            }
        }
    }

    private fun pauseTimer() {
        _uiState.value = _uiState.value.copy(isRunning = false)
        timerJob?.cancel()
        updateNotification(isSilent = true)
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetTimer() {
        _uiState.value = TimerUiState(
            timeLeftInSeconds = WORK_TIME,
            totalTimeInSeconds = WORK_TIME,
            currentSession = 1,
            phase = TimerPhase.WORK
        )
        timerJob?.cancel()
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopSelf()
    }

    private fun createNotification(isSilent: Boolean = true): Notification {
        val state = _uiState.value
        val minutes = state.timeLeftInSeconds / 60
        val seconds = state.timeLeftInSeconds % 60
        val timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

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
            .setSilent(isSilent)

        if (state.isRunning) {
            val pauseIntent = Intent(this, TimerService::class.java).apply { action = ACTION_PAUSE }
            val pausePendingIntent =
                PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            builder.addAction(0, "PAUSE", pausePendingIntent)
        } else if (state.phase != TimerPhase.FINISHED) {
            val resumeIntent =
                Intent(this, TimerService::class.java).apply { action = ACTION_START }
            val resumePendingIntent =
                PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            builder.addAction(0, "RESUME", resumePendingIntent)
        }

        val resetIntent = Intent(this, TimerService::class.java).apply { action = ACTION_RESET }
        val resetPendingIntent =
            PendingIntent.getService(this, 3, resetIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        builder.addAction(0, "RESET", resetPendingIntent)

        return builder.build()
    }

    private fun updateNotification(isSilent: Boolean = true) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(isSilent))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for Pomodoro session transitions"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
