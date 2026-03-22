package com.minimalist.launcher.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val category: String = "All"
)

data class HabitItem(
    val id: String,
    val name: String,
    val emoji: String,
    val isDoneToday: Boolean = false,
    val streakDays: Int = 0
)

data class Note(
    val id: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class FocusState {
    IDLE, RUNNING, PAUSED, BREAK
}

data class FocusSession(
    val durationMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val state: FocusState = FocusState.IDLE,
    val remainingSeconds: Int = 25 * 60,
    val sessionsCompleted: Int = 0
)
