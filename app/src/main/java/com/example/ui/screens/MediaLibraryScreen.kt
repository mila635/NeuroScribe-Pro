package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ui.components.SpreadedAnimatedBackground
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MediaItem
import com.example.ui.viewmodel.MediaType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Media Library Screen.
 * Provides unified, professional access to all audio recordings and visual documents captured in the app.
 */
@Composable
fun MediaLibraryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val mediaItems by viewModel.mediaItems.collectAsState()
    val isPlaying by viewModel.playerManager.isPlaying.collectAsState()
    val currentPositionMs by viewModel.playerManager.currentPositionMs.collectAsState()
    val totalDurationMs by viewModel.playerManager.totalDurationMs.collectAsState()
    val currentPlayingPath by viewModel.playerManager.currentPlayingPath.collectAsState()

    var filterType by remember { mutableIntStateOf(0) } // 0: All, 1: Audio, 2: Images
    var previewImageItem by remember { mutableStateOf<MediaItem?>(null) }
    var itemToDelete by remember { mutableStateOf<MediaItem?>(null) }

    val audioCount = mediaItems.count { it.type == MediaType.AUDIO }
    val imageCount = mediaItems.count { it.type == MediaType.IMAGE }

    val filteredList = when (filterType) {
        1 -> mediaItems.filter { it.type == MediaType.AUDIO }
        2 -> mediaItems.filter { it.type == MediaType.IMAGE }
        else -> mediaItems
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    SpreadedAnimatedBackground(
        modifier = modifier.fillMaxSize(),
        isDarkTheme = isDarkTheme
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Media Library",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$audioCount audio recordings • $imageCount visual documents",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PermMedia,
                        contentDescription = "Media Library",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (All, Audio, Visual)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == 0,
                    onClick = { filterType = 0 },
                    label = { Text("All Media (${mediaItems.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor.copy(alpha = 0.18f),
                        selectedLabelColor = primaryColor
                    ),
                    modifier = Modifier.testTag("filter_all_media")
                )

                FilterChip(
                    selected = filterType == 1,
                    onClick = { filterType = 1 },
                    label = { Text("Audio ($audioCount)", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor.copy(alpha = 0.18f),
                        selectedLabelColor = primaryColor
                    ),
                    modifier = Modifier.testTag("filter_audio_media")
                )

                FilterChip(
                    selected = filterType == 2,
                    onClick = { filterType = 2 },
                    label = { Text("Visual ($imageCount)", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = secondaryColor.copy(alpha = 0.18f),
                        selectedLabelColor = secondaryColor
                    ),
                    modifier = Modifier.testTag("filter_visual_media")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Media Items List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PermMedia,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "No media files yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Record an audio note or capture a lecture slide to populate your library.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { viewModel.navigateTo(AppScreen.CAPTURE) }
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record Audio Now")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        if (item.type == MediaType.AUDIO) {
                            AudioMediaCard(
                                item = item,
                                isCurrentlyPlaying = isPlaying && currentPlayingPath == item.filePath,
                                isCurrentTrack = currentPlayingPath == item.filePath,
                                currentPositionMs = currentPositionMs,
                                totalDurationMs = totalDurationMs,
                                onPlayPause = { viewModel.playMediaAudio(item.filePath) },
                                onOpenNote = { item.noteId?.let { viewModel.openNoteForMedia(it) } },
                                onShare = { shareMediaFile(context, item.filePath, item.title, "audio/*") },
                                onDelete = { itemToDelete = item }
                            )
                        } else {
                            ImageMediaCard(
                                item = item,
                                onClick = { previewImageItem = item },
                                onOpenNote = { item.noteId?.let { viewModel.openNoteForMedia(it) } },
                                onShare = { shareMediaFile(context, item.filePath, item.title, "image/*") },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }
                }
            }
        }

        // Full Screen Image Preview Modal
        previewImageItem?.let { imageItem ->
            AlertDialog(
                onDismissRequest = { previewImageItem = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = imageItem.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        IconButton(onClick = { previewImageItem = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Preview")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val file = File(imageItem.filePath)
                        if (file.exists()) {
                            AsyncImage(
                                model = file,
                                contentDescription = imageItem.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${file.length() / 1024} KB • ${SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(imageItem.timestamp))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    if (imageItem.noteId != null) {
                        TextButton(
                            onClick = {
                                previewImageItem = null
                                viewModel.openNoteForMedia(imageItem.noteId)
                            }
                        ) {
                            Text("Open Linked Note")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { previewImageItem = null }) {
                        Text("Dismiss")
                    }
                }
            )
        }

        // Delete Confirmation Modal
        itemToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("Delete Media File?") },
                text = { Text("This will permanently remove '${item.title}' from local storage.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteMediaItem(item)
                            itemToDelete = null
                            Toast.makeText(context, "Media deleted", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun AudioMediaCard(
    item: MediaItem,
    isCurrentlyPlaying: Boolean,
    isCurrentTrack: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    onPlayPause: () -> Unit,
    onOpenNote: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val formattedDate = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(item.timestamp))
    val durationText = formatDuration(if (isCurrentTrack && totalDurationMs > 0) totalDurationMs else item.durationMs)
    val sizeText = "${item.fileSizeBytes / 1024} KB"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audio_media_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrentlyPlaying) primaryColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentlyPlaying) primaryColor else primaryColor.copy(alpha = 0.12f))
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCurrentlyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isCurrentlyPlaying) "Pause" else "Play",
                        tint = if (isCurrentlyPlaying) Color.White else primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "$durationText • $sizeText • $formattedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                // Actions: Share & Delete
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Audio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Audio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Playback progress bar if current track
            if (isCurrentTrack && (totalDurationMs > 0 || item.durationMs > 0)) {
                val effectiveTotal = if (totalDurationMs > 0) totalDurationMs else item.durationMs
                val progress = (currentPositionMs.toFloat() / effectiveTotal.toFloat()).coerceIn(0f, 1f)
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = primaryColor,
                    trackColor = primaryColor.copy(alpha = 0.15f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(currentPositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(effectiveTotal),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Jump to linked note button
            if (item.noteId != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOpenNote() }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Open Cognitive Dossier",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageMediaCard(
    item: MediaItem,
    onClick: () -> Unit,
    onOpenNote: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val formattedDate = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(item.timestamp))
    val sizeText = "${item.fileSizeBytes / 1024} KB"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("image_media_card_${item.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            val file = File(item.filePath)
            if (file.exists()) {
                AsyncImage(
                    model = file,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(secondaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = secondaryColor)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "$sizeText • $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                if (item.noteId != null) {
                    Text(
                        text = "Linked to: ${item.noteTitle ?: "Note"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Share & Delete
            IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSec = durationMs / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", min, sec)
}

private fun shareMediaFile(context: android.content.Context, filePath: String, title: String, mimeType: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            return
        }
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing media: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
