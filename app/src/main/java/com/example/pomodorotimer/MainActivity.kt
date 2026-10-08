package com.example.pomodorotimer

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.pomodorotimer.data.UserPreferencesRepository
import com.example.pomodorotimer.theme.PomodoroTimerTheme
import com.example.pomodorotimer.ui.components.OnboardingScreen
import com.example.pomodorotimer.ui.timer.MainTimerScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var userPreferencesRepository: UserPreferencesRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      PomodoroTimerTheme {
        val launcher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission(),
          onResult = { isGranted -> /* Handle result if needed */ }
        )
        LaunchedEffect(Unit) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
          }
        }
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
         ) {
           AppNavigation(userPreferencesRepository)
        }
      }
    }
  }

  @Composable
  fun AppNavigation(userPrefRepository: UserPreferencesRepository) {
    val hasSeenOnboarding by userPrefRepository.hasSeenOnboarding.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    // Wait until we load the preference from disk
    if (hasSeenOnboarding == null) return

    val startDestination = if (hasSeenOnboarding == true) Main else Onboarding
    val backStack = rememberNavBackStack(startDestination)

    NavDisplay(
      backStack = backStack,
      onBack = { backStack.removeLastOrNull() },
      entryProvider = entryProvider {
        entry<Onboarding> {
          OnboardingScreen(
            onFinish = {
              coroutineScope.launch {
                userPrefRepository.saveOnboardingState(true)
              }
              // Navigate to Main Timer
              backStack.clear()
              backStack.add(Main)
            }
          )
        }
        entry<Main> {
          MainTimerScreen()
        }
      }
    )
  }
}