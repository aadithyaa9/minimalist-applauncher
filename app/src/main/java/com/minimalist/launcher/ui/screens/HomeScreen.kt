package com.minimalist.launcher.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalist.launcher.data.LauncherViewModel
import com.minimalist.launcher.ui.components.*
import com.minimalist.launcher.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val currentTime by viewModel.currentTime.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val greeting by viewModel.greeting.collectAsState()
    val pinnedApps by viewModel.pinnedApps.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 4 })

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setActiveTab(pagerState.currentPage)
    }
    LaunchedEffect(activeTab) {
        if (pagerState.currentPage != activeTab) {
            pagerState.animateScrollToPage(activeTab)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount < -50f) {
                            viewModel.setShowAllApps(true)
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 56.dp, bottom = 24.dp)
        ) {
            // Clock + greeting
            TimeBlock(time = currentTime, date = currentDate, greeting = greeting)

            Spacer(modifier = Modifier.height(32.dp))

            // Tab indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                listOf("HOME", "FOCUS", "HABITS", "SCREEN").forEachIndexed { i, label ->
                    val isActive = activeTab == i
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 2.sp,
                        color = if (isActive) AccentGreen else TextTertiary,
                        modifier = Modifier.clickable { viewModel.setActiveTab(i) }
                    )
                    if (i < 3) {
                        Text(
                            text = "·",
                            fontSize = 10.sp,
                            color = TextTertiary,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> HomeTab(viewModel = viewModel)
                    1 -> FocusTab(viewModel = viewModel)
                    2 -> HabitsTab(viewModel = viewModel)
                    3 -> ScreenTimeTab(viewModel = viewModel)
                }
            }

            // Pinned apps dock
            if (pinnedApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                PinnedDock(apps = pinnedApps, onLaunch = { viewModel.launchApp(context, it) })
            }

            // Swipe up hint
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "↑ all apps",
                fontSize = 11.sp,
                color = TextTertiary,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setShowAllApps(true) }
            )
        }
    }
}

@Composable
fun TimeBlock(time: String, date: String, greeting: String) {
    Column {
        Text(
            text = time,
            style = MaterialTheme.typography.displayLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Thin,
                fontSize = 72.sp,
                letterSpacing = (-3).sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = date.uppercase(),
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = greeting,
            fontSize = 16.sp,
            color = AccentGreen,
            fontWeight = FontWeight.Light,
            letterSpacing = 0.5.sp
        )
    }
}
