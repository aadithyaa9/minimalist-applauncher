package com.minimalist.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.minimalist.launcher.ui.LauncherApp
import com.minimalist.launcher.ui.theme.LauncherTheme
import com.minimalist.launcher.data.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LauncherTheme {
                // Prevent the launcher from closing when back is pressed
                BackHandler {
                    if (viewModel.showAllApps.value) {
                        viewModel.setShowAllApps(false)
                    } else if (viewModel.activeTab.value != 0) {
                        viewModel.setActiveTab(0)
                    }
                    // Otherwise, do nothing (stay on home)
                }
                
                Surface(modifier = Modifier.fillMaxSize()) {
                    LauncherApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure data is fresh when returning home
        viewModel.loadApps(this)
        viewModel.updateTime()
    }
}
