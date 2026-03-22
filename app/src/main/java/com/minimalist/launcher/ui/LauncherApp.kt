package com.minimalist.launcher.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.minimalist.launcher.data.LauncherViewModel
import com.minimalist.launcher.ui.screens.AllAppsScreen
import com.minimalist.launcher.ui.screens.HomeScreen

@Composable
fun LauncherApp(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val showAllApps by viewModel.showAllApps.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadApps(context)
    }

    if (showAllApps) {
        AllAppsScreen(viewModel = viewModel)
    } else {
        HomeScreen(viewModel = viewModel)
    }
}
