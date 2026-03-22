package com.minimalist.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Pure Minimalist Grayscale Palette
val Black = Color(0xFF000000)
val Surface1 = Color(0xFF0A0A0A)
val Surface2 = Color(0xFF121212)
val Surface3 = Color(0xFF1A1A1A)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFAAAAAA)
val TextTertiary = Color(0xFF555555)

// Changed Accent to a neutral Gray to remove all "app colors"
val AccentGreen = Color(0xFFEEEEEE) 
val AccentDim = Color(0xFF333333)
val DividerColor = Color(0xFF1A1A1A)

val LauncherColors = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Black,
    background = Black,
    surface = Surface1,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    surfaceVariant = Surface2,
    secondary = TextSecondary,
    onSecondary = Black,
    tertiary = TextTertiary,
    onBackground = TextPrimary,
)

@Composable
fun LauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LauncherColors,
        typography = LauncherTypography,
        content = content
    )
}

val LauncherTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Thin,
        fontSize = 72.sp,
        letterSpacing = (-3).sp,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 48.sp,
        letterSpacing = (-2).sp,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 32.sp,
        letterSpacing = (-1).sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = TextSecondary
    )
)
