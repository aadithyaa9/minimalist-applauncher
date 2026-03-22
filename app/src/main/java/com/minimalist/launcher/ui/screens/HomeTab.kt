package com.minimalist.launcher.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalist.launcher.data.LauncherViewModel
import com.minimalist.launcher.data.Note
import com.minimalist.launcher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeTab(viewModel: LauncherViewModel) {
    val scratchNote by viewModel.scratchNote.collectAsState()
    val notes by viewModel.notes.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionLabel("SCRATCH PAD")
            Spacer(modifier = Modifier.height(8.dp))
            ScratchPadInput(
                value = scratchNote,
                onValueChange = { viewModel.updateScratchNote(it) },
                onSave = { viewModel.saveNote() }
            )
        }

        if (notes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("SAVED NOTES")
                }
            }
            items(notes, key = { it.id }) { note ->
                NoteCard(note = note, onDelete = { viewModel.deleteNote(note.id) })
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun ScratchPadInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface2)
            .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(
                color = TextPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Light
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 100.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        "Type to remember...",
                        color = TextTertiary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    )
                }
                inner()
            }
        )
        
        AnimatedVisibility(
            visible = value.trim().isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DividerColor)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SAVE NOTE",
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSave() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface1)
            .border(0.5.dp, DividerColor, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.content,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sdf.format(Date(note.timestamp)),
                    fontSize = 10.sp,
                    color = TextTertiary,
                    letterSpacing = 0.5.sp
                )
            }
            if (isExpanded) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextTertiary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        letterSpacing = 2.sp,
        color = TextTertiary,
        fontWeight = FontWeight.Medium
    )
}
