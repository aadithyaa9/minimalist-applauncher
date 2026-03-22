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
                SectionLabel("SAVED NOTES")
            }
            items(notes) { note ->
                NoteCard(note = note, onDelete = { viewModel.deleteNote(note.id) })
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
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
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .padding(16.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(
                color = TextPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 80.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        "Write anything...",
                        color = TextTertiary,
                        fontSize = 15.sp
                    )
                }
                inner()
            }
        )
        if (value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SAVE",
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                    modifier = Modifier.clickable { onSave() }
                )
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface1)
            .border(1.dp, DividerColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.content,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sdf.format(Date(note.timestamp)),
                fontSize = 11.sp,
                color = TextTertiary,
                letterSpacing = 0.5.sp
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete",
                tint = TextTertiary,
                modifier = Modifier.size(16.dp)
            )
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
