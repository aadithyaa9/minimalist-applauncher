package com.minimalist.launcher.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalist.launcher.data.HabitItem
import com.minimalist.launcher.data.LauncherViewModel
import com.minimalist.launcher.ui.theme.*

@Composable
fun HabitsTab(viewModel: LauncherViewModel) {
    val habits by viewModel.habits.collectAsState()
    val doneCount = habits.count { it.isDoneToday }
    var showAddHabit by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var newHabitEmoji by remember { mutableStateOf("✨") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Progress summary
        val pct = if (habits.isEmpty()) 0f else doneCount.toFloat() / habits.size
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$doneCount / ${habits.size}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Thin,
                    color = TextPrimary,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = if (pct == 1f) "All done! 🎉" else "today's habits",
                    fontSize = 12.sp,
                    color = if (pct == 1f) AccentGreen else TextSecondary
                )
            }
            // Mini progress arc
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                LinearProgressIndicator(
                    progress = { pct },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentGreen,
                    trackColor = Surface2
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
            color = AccentGreen,
            trackColor = Surface2
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(habits) { habit ->
                HabitRow(
                    habit = habit,
                    onToggle = { viewModel.toggleHabit(habit.id) },
                    onDelete = { viewModel.deleteHabit(habit.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add habit
        if (showAddHabit) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface2)
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = newHabitEmoji,
                    onValueChange = { if (it.length <= 2) newHabitEmoji = it },
                    textStyle = LocalTextStyle.current.copy(
                        color = TextPrimary,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.width(32.dp)
                )
                BasicTextField(
                    value = newHabitName,
                    onValueChange = { newHabitName = it },
                    textStyle = LocalTextStyle.current.copy(
                        color = TextPrimary,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (newHabitName.isEmpty()) Text("Habit name...", color = TextTertiary, fontSize = 15.sp)
                        inner()
                    }
                )
                Text(
                    text = "ADD",
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                    modifier = Modifier.clickable {
                        viewModel.addHabit(newHabitName, newHabitEmoji)
                        newHabitName = ""
                        newHabitEmoji = "✨"
                        showAddHabit = false
                    }
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { showAddHabit = true }
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("add habit", fontSize = 13.sp, color = TextTertiary)
            }
        }
    }
}

@Composable
fun HabitRow(habit: HabitItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (habit.isDoneToday) AccentDim else Surface1)
            .border(
                1.dp,
                if (habit.isDoneToday) AccentGreen.copy(alpha = 0.3f) else DividerColor,
                RoundedCornerShape(10.dp)
            )
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(habit.emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name,
                fontSize = 15.sp,
                color = if (habit.isDoneToday) AccentGreen else TextPrimary,
                fontWeight = if (habit.isDoneToday) FontWeight.Medium else FontWeight.Normal
            )
            if (habit.streakDays > 0) {
                Text(
                    text = "🔥 ${habit.streakDays} day streak",
                    fontSize = 11.sp,
                    color = if (habit.isDoneToday) AccentGreen.copy(alpha = 0.7f) else TextTertiary
                )
            }
        }
        // Checkbox
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (habit.isDoneToday) AccentGreen else Surface3)
                .border(1.dp, if (habit.isDoneToday) AccentGreen else TextTertiary, RoundedCornerShape(6.dp))
        ) {
            if (habit.isDoneToday) {
                Text("✓", fontSize = 14.sp, color = Black, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Delete", tint = TextTertiary, modifier = Modifier.size(14.dp))
        }
    }
}
