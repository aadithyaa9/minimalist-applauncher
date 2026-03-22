package com.minimalist.launcher.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalist.launcher.data.AppInfo
import com.minimalist.launcher.data.LauncherViewModel
import com.minimalist.launcher.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllAppsScreen(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val query by viewModel.searchQuery.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val hiddenPackages by viewModel.hiddenPackages.collectAsState()

    var appToLaunch by remember { mutableStateOf<AppInfo?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker && appToLaunch != null) {
        TimeSelectionDialog(
            appName = appToLaunch!!.name,
            onDismiss = {
                showTimePicker = false
                appToLaunch = null
            },
            onConfirm = { minutes ->
                viewModel.launchApp(context, appToLaunch!!.packageName, minutes)
                showTimePicker = false
                appToLaunch = null
            }
        )
    }

    val filteredApps = remember(query, allApps, hiddenPackages) {
        val q = query.lowercase().trim()
        if (q.isEmpty()) {
            allApps.filter { !hiddenPackages.contains(it.packageName) }
        } else {
            allApps.filter {
                it.name.lowercase().contains(q) && !hiddenPackages.contains(it.packageName)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 80f) {
                            viewModel.setShowAllApps(false)
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "APPS",
                    fontSize = 11.sp,
                    letterSpacing = 3.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "↓ back",
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = TextTertiary,
                    modifier = Modifier.clickable { viewModel.setShowAllApps(false) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface2)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    textStyle = LocalTextStyle.current.copy(
                        color = TextPrimary,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(AccentGreen),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("Search apps...", color = TextTertiary, fontSize = 16.sp)
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App count
            Text(
                text = "${filteredApps.size} apps",
                fontSize = 11.sp,
                color = TextTertiary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // App list - text only
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredApps, key = { "${it.packageName}_${it.name}" }) { app ->
                    AppRow(
                        app = app,
                        isPinned = viewModel.isPinned(app.packageName),
                        onLaunch = {
                            appToLaunch = app
                            showTimePicker = true
                        },
                        onTogglePin = { viewModel.togglePin(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun TimeSelectionDialog(
    appName: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var customMinutes by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface1,
        title = {
            Text(
                text = "Intentional usage",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "How much time for $appName?",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick options
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(5, 15, 30).forEach { mins ->
                        Text(
                            text = "${mins}m",
                            fontSize = 13.sp,
                            color = AccentGreen,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Surface2)
                                .clickable { onConfirm(mins) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Custom input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Surface2)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = customMinutes,
                        onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) customMinutes = it },
                        textStyle = LocalTextStyle.current.copy(
                            color = TextPrimary,
                            fontSize = 14.sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        cursorBrush = SolidColor(AccentGreen),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (customMinutes.isEmpty()) Text("Custom...", color = TextTertiary, fontSize = 14.sp)
                            inner()
                        }
                    )
                    if (customMinutes.isNotEmpty()) {
                        Text(
                            text = "START",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen,
                            letterSpacing = 1.sp,
                            modifier = Modifier.clickable {
                                customMinutes.toIntOrNull()?.let { onConfirm(it) }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextTertiary, letterSpacing = 1.sp, fontSize = 11.sp)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppRow(
    app: AppInfo,
    isPinned: Boolean,
    onLaunch: () -> Unit,
    onTogglePin: () -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = { showOptions = !showOptions }
            )
            .padding(vertical = 13.dp, horizontal = 4.dp)
    ) {
        Text(
            text = app.name,
            fontSize = 17.sp,
            color = if (isPinned) AccentGreen else TextPrimary,
            fontWeight = FontWeight.Light,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f)
        )
        if (showOptions) {
            Text(
                text = if (isPinned) "unpin" else "pin",
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = AccentGreen,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AccentDim)
                    .clickable {
                        onTogglePin()
                        showOptions = false
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }

    // thin divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(DividerColor)
    )
}
