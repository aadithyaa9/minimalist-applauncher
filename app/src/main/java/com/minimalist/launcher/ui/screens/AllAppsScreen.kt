package com.minimalist.launcher.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val filteredApps = remember(query, viewModel.allApps.collectAsState().value) {
        viewModel.getFilteredApps()
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
                items(filteredApps, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        isPinned = viewModel.isPinned(app.packageName),
                        onLaunch = { viewModel.launchApp(context, app.packageName) },
                        onTogglePin = { viewModel.togglePin(app) }
                    )
                }
            }
        }
    }
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
