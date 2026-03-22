package com.minimalist.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalist.launcher.data.AppInfo
import com.minimalist.launcher.ui.theme.*

@Composable
fun PinnedDock(apps: List<AppInfo>, onLaunch: (String) -> Unit) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
    ) {
        drawLine(
            color = DividerColor,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1f
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        apps.forEach { app ->
            Text(
                text = app.name,
                fontSize = 13.sp,
                color = AccentGreen,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable { onLaunch(app.packageName) }
                    .weight(1f, fill = false)
            )
        }
    }
}
