package com.example.pomodorotimer.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodorotimer.theme.PressStart2PFontFamily
import com.example.pomodorotimer.theme.RetroGreen
import com.example.pomodorotimer.theme.RetroGreenButton
import com.example.pomodorotimer.theme.RetroGreenDark
import com.example.pomodorotimer.theme.RetroOrange
import com.example.pomodorotimer.theme.RetroYellow
import kotlinx.coroutines.launch


data class OnboardingPageData(
    val title: String,
    val description: String,
    val animation: @Composable () -> Unit
)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit){
    val pages = listOf(
        OnboardingPageData(
            "TIME BLOCKING",
            "Working in time blocks is an effective strategy for using time wisely.",
            { AnimatedBouncingTomato() }
        ),
        OnboardingPageData(
            "HOW IT WORKS",
            "Choose a task. Eliminate distractions. Set timer for 25 mins.",
            { /* Add Clock Animation */ }
        ),
        OnboardingPageData(
            "STAY FOCUSED",
            "Give your work your undivided attention. The world can wait.",
            { AnimatedBlinkingMonitor() }
        ),
        // ... Add the remaining 2 pages here
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    RetroBorder {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Render the specific animation for this page
                pages[page].animation()

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = pages[page].title,
                    color = RetroYellow,
                    fontSize = 16.sp,
                    fontFamily = PressStart2PFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = pages[page].description,
                    color = RetroGreen, // Glowing retro text
                    fontSize = 10.sp,
                    fontFamily = PressStart2PFontFamily,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }

        // Bottom Controls (Indicators & Button)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            // Retro Page Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                repeat(pages.size) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) RetroYellow else RetroGreenDark
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color)
                    )
                }
            }

            // Buttons
            if (pagerState.currentPage == pages.lastIndex) {
                RetroButton(
                    text = "START TIMER",
                    onClick = { onFinish() },
                    containerColor = RetroGreenButton
                )
            } else {
                RetroButton(
                    text = "NEXT",
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    containerColor = RetroOrange
                )
            }
        }
    }
}