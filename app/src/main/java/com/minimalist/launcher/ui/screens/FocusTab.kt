package com.minimalist.launcher.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalist.launcher.data.FocusSession
import com.minimalist.launcher.data.FocusState
import com.minimalist.launcher.data.LauncherViewModel
import com.minimalist.launcher.ui.theme.*

@Composable
fun FocusTab(viewModel: LauncherViewModel) {
    val session by viewModel.focusSession.collectAsState()
    var showDurationPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Session counter
        Text(
            text = "SESSION ${session.sessionsCompleted + 1}",
            fontSize = 10.sp,
            letterSpacing = 3.sp,
            color = TextTertiary,
            fontWeight = FontWeight.Medium
        )

        if (session.sessionsCompleted > 0) {
            Text(
                text = "✓ ${session.sessionsCompleted} completed today",
                fontSize = 12.sp,
                color = AccentGreen,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timer ring
        val totalSeconds = if (session.state == FocusState.BREAK) {
            session.breakMinutes * 60f
        } else {
            session.durationMinutes * 60f
        }
        val progress = if (totalSeconds > 0) session.remainingSeconds / totalSeconds else 1f

        Box(contentAlignment = Alignment.Center) {
            TimerRing(progress = progress, state = session.state)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val mins = session.remainingSeconds / 60
                val secs = session.remainingSeconds % 60
                Text(
                    text = String.format("%02d:%02d", mins, secs),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = (-2).sp,
                    color = TextPrimary
                )
                Text(
                    text = if (session.state == FocusState.BREAK) "BREAK" else "FOCUS",
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                    color = if (session.state == FocusState.BREAK) TextSecondary else AccentGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controls
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            when (session.state) {
                FocusState.IDLE, FocusState.PAUSED -> {
                    FocusButton(
                        label = if (session.state == FocusState.PAUSED) "RESUME" else "START",
                        isPrimary = true,
                        onClick = { viewModel.startFocus() }
                    )
                    if (session.state == FocusState.PAUSED) {
                        FocusButton(label = "RESET", isPrimary = false) { viewModel.resetFocus() }
                    }
                }
                FocusState.RUNNING -> {
                    FocusButton(label = "PAUSE", isPrimary = true, onClick = { viewModel.pauseFocus() })
                    FocusButton(label = "RESET", isPrimary = false, onClick = { viewModel.resetFocus() })
                }
                FocusState.BREAK -> {
                    FocusButton(label = "SKIP BREAK", isPrimary = false, onClick = { viewModel.resetFocus() })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Duration presets
        if (session.state == FocusState.IDLE) {
            SectionLabel("DURATION")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15, 25, 45, 60).forEach { mins ->
                    val isSelected = session.durationMinutes == mins
                    Text(
                        text = "${mins}m",
                        fontSize = 13.sp,
                        color = if (isSelected) AccentGreen else TextTertiary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) AccentDim else Surface2)
                            .clickable { viewModel.setFocusDuration(mins) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimerRing(progress: Float, state: FocusState) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "progress"
    )
    val ringColor = if (state == FocusState.BREAK) TextSecondary else AccentGreen

    Canvas(modifier = Modifier.size(220.dp)) {
        val strokeWidth = 6.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)

        // Background ring
        drawArc(
            color = Surface2,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(radius * 2, radius * 2),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        // Progress ring
        drawArc(
            color = ringColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = Size(radius * 2, radius * 2),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun FocusButton(label: String, isPrimary: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPrimary) AccentGreen else Surface2)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) Black else TextSecondary
        )
    }
}
