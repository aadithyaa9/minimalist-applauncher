package com.minimalist.launcher.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalist.launcher.data.LauncherViewModel
import com.minimalist.launcher.ui.theme.*

@Composable
fun ScreenTimeTab(viewModel: LauncherViewModel) {
    val allApps by viewModel.allApps.collectAsState()
    val launchCounts by viewModel.appLaunchCounts.collectAsState()
    val blockedPackages by viewModel.blockedPackages.collectAsState()

    // Sort by launch count descending, only show apps that have been launched or are blocked
    val trackedApps = remember(allApps, launchCounts, blockedPackages) {
        allApps.filter { launchCounts.containsKey(it.packageName) || blockedPackages.contains(it.packageName) }
            .sortedByDescending { launchCounts[it.packageName] ?: 0 }
    }

    val totalLaunches = launchCounts.values.sum()

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$totalLaunches",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Thin,
                    color = TextPrimary,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = "app opens this session",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            if (totalLaunches > 0) {
                Text(
                    text = "CLEAR",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = TextTertiary,
                    modifier = Modifier.clickable { viewModel.clearLaunchHistory() }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (trackedApps.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No app launches tracked yet.\nOpen apps to see them here.",
                fontSize = 14.sp,
                color = TextTertiary,
                lineHeight = 22.sp
            )
        } else {
            SectionLabel("USAGE  ·  LONG PRESS TO BLOCK")
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(trackedApps, key = { it.packageName }) { app ->
                    val count = launchCounts[app.packageName] ?: 0
                    val maxCount = launchCounts.values.maxOrNull() ?: 1
                    val isBlocked = blockedPackages.contains(app.packageName)

                    UsageRow(
                        appName = app.name,
                        count = count,
                        maxCount = maxCount,
                        isBlocked = isBlocked,
                        onToggleBlock = { viewModel.toggleBlock(app.packageName) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Blocked apps section
        if (blockedPackages.isNotEmpty()) {
            val blockedNames = allApps.filter { blockedPackages.contains(it.packageName) }
            SectionLabel("BLOCKED (${blockedNames.size})")
            Spacer(modifier = Modifier.height(8.dp))
            blockedNames.forEach { app ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = app.name,
                        fontSize = 14.sp,
                        color = TextTertiary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "unblock",
                        fontSize = 11.sp,
                        color = AccentGreen,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentDim)
                            .clickable { viewModel.toggleBlock(app.packageName) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UsageRow(
    appName: String,
    count: Int,
    maxCount: Int,
    isBlocked: Boolean,
    onToggleBlock: () -> Unit
) {
    val pct = if (maxCount > 0) count.toFloat() / maxCount else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onToggleBlock
            )
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appName,
                fontSize = 14.sp,
                color = if (isBlocked) TextTertiary else TextPrimary,
                fontWeight = FontWeight.Light
            )
            Text(
                text = "$count ×",
                fontSize = 12.sp,
                color = if (count > 5) AccentGreen else TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Surface2)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (count > 5) AccentGreen.copy(alpha = 0.7f) else TextTertiary)
            )
        }
    }
}
