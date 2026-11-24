package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DetectedLanguage
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

enum class DockAnchorSide {
    LEFT, RIGHT, TOP, BOTTOM
}

private data class RainbowArcAction(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val gradientColors: List<Color>,
    val onClick: () -> Unit
)

/**
 * Universal Rainbow Arc Floating Side Dock.
 * - Anchors strictly to screen edges (Left, Right, Top, Bottom).
 * - If dragged into center, forcefully and automatically snaps to nearest edge.
 * - Options fan out in a curved circular arc like a rainbow.
 * - Sleek neural orb icon with glowing pulse.
 */
@Composable
fun FloatingActionSideDock(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val dockSizePx = with(density) { 56.dp.toPx() }

    // Position State (Default to right edge)
    var currentSide by remember { mutableStateOf(DockAnchorSide.RIGHT) }
    var targetX by remember { mutableFloatStateOf(screenWidthPx - dockSizePx - with(density) { 10.dp.toPx() }) }
    var targetY by remember { mutableFloatStateOf(screenHeightPx * 0.45f) }

    var isDragging by remember { mutableStateOf(false) }
    var dragRawX by remember { mutableFloatStateOf(targetX) }
    var dragRawY by remember { mutableFloatStateOf(targetY) }

    var isExpanded by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLanguage by viewModel.selectedLanguage.collectAsState()

    // Smooth magnetic edge snapping animation
    val animatedX by animateFloatAsState(
        targetValue = if (isDragging) dragRawX else targetX,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "dock_x"
    )
    val animatedY by animateFloatAsState(
        targetValue = if (isDragging) dragRawY else targetY,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "dock_y"
    )

    // Resting alpha: 0.48f when idle, 1.0f when dragging or expanded
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isExpanded || isDragging) 1.0f else 0.48f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "dock_alpha"
    )

    // Function to calculate nearest side and forcefully snap
    fun snapToNearestSide(dropX: Float, dropY: Float) {
        val dLeft = dropX
        val dRight = screenWidthPx - dockSizePx - dropX
        val dTop = dropY
        val dBottom = screenHeightPx - dockSizePx - dropY

        val minDistance = minOf(dLeft, dRight, dTop, dBottom)
        val edgePadding = with(density) { 10.dp.toPx() }
        val topBound = with(density) { 70.dp.toPx() }
        val bottomBound = screenHeightPx - dockSizePx - with(density) { 100.dp.toPx() }

        when (minDistance) {
            dLeft -> {
                targetX = edgePadding
                targetY = dropY.coerceIn(topBound, bottomBound)
                currentSide = DockAnchorSide.LEFT
            }
            dRight -> {
                targetX = screenWidthPx - dockSizePx - edgePadding
                targetY = dropY.coerceIn(topBound, bottomBound)
                currentSide = DockAnchorSide.RIGHT
            }
            dTop -> {
                targetY = topBound
                targetX = dropX.coerceIn(edgePadding, screenWidthPx - dockSizePx - edgePadding)
                currentSide = DockAnchorSide.TOP
            }
            else -> {
                targetY = bottomBound
                targetX = dropX.coerceIn(edgePadding, screenWidthPx - dockSizePx - edgePadding)
                currentSide = DockAnchorSide.BOTTOM
            }
        }
    }

    // 7 Rainbow Arc Actions
    val actions = listOf(
        RainbowArcAction(
            id = "record",
            icon = Icons.Default.Mic,
            label = "Record",
            gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
            onClick = {
                isExpanded = false
                viewModel.navigateTo(AppScreen.CAPTURE)
            }
        ),
        RainbowArcAction(
            id = "archive",
            icon = Icons.Default.Archive,
            label = "Archives",
            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
            onClick = {
                isExpanded = false
                viewModel.navigateTo(AppScreen.ARCHIVE)
            }
        ),
        RainbowArcAction(
            id = "media",
            icon = Icons.Default.PermMedia,
            label = "Media",
            gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF0891B2)),
            onClick = {
                isExpanded = false
                viewModel.navigateTo(AppScreen.MEDIA_LIBRARY)
            }
        ),
        RainbowArcAction(
            id = "retention",
            icon = Icons.Default.Psychology,
            label = "Recall",
            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
            onClick = {
                isExpanded = false
                viewModel.navigateTo(AppScreen.RETENTION)
            }
        ),
        RainbowArcAction(
            id = "language",
            icon = Icons.Default.Translate,
            label = "Language",
            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
            onClick = {
                isExpanded = false
                showLanguageDialog = true
            }
        ),
        RainbowArcAction(
            id = "camera",
            icon = Icons.Default.CameraAlt,
            label = "Camera",
            gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)),
            onClick = {
                isExpanded = false
                viewModel.triggerCameraLaunch()
            }
        ),
        RainbowArcAction(
            id = "close",
            icon = Icons.Default.Close,
            label = "Close",
            gradientColors = listOf(Color(0xFF64748B), Color(0xFF475569)),
            onClick = { isExpanded = false }
        )
    )

    // Language Picker Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text("Select Global Language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    items(DetectedLanguage.entries) { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentLanguage == lang) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    viewModel.setSelectedLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(lang.flagEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(lang.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(lang.nativeLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (currentLanguage == lang) {
                                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Container placed at animated position
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = animatedX.roundToInt(),
                        y = animatedY.roundToInt()
                    )
                }
                .alpha(animatedAlpha)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragRawX = targetX
                            dragRawY = targetY
                        },
                        onDragEnd = {
                            isDragging = false
                            snapToNearestSide(dragRawX, dragRawY)
                        },
                        onDragCancel = {
                            isDragging = false
                            snapToNearestSide(dragRawX, dragRawY)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragRawX += dragAmount.x
                            dragRawY += dragAmount.y
                        }
                    )
                }
        ) {
            // Main Trigger Neural Orb Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6),
                                Color(0xFFEC4899),
                                Color(0xFFF59E0B),
                                Color(0xFF10B981),
                                Color(0xFF06B6D4),
                                Color(0xFF6366F1)
                            )
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                    .shadow(12.dp, CircleShape)
                    .clickable { isExpanded = !isExpanded }
                    .testTag("floating_dock_trigger"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E1B4B),
                                    Color(0xFF312E81)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.AutoAwesome,
                        contentDescription = "Quick Actions Rainbow Dock",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Rainbow Arc Fan of Actions
            if (isExpanded) {
                val radius = with(density) { 108.dp.toPx() }
                val count = actions.size
                val arcSpanRad = 150.0 * (PI / 180.0) // 150 degrees arc span

                // Start angle depending on which edge we are docked to
                val baseAngleRad = when (currentSide) {
                    DockAnchorSide.RIGHT -> PI // points leftward (180 deg)
                    DockAnchorSide.LEFT -> 0.0 // points rightward (0 deg)
                    DockAnchorSide.TOP -> PI / 2.0 // points downward (90 deg)
                    DockAnchorSide.BOTTOM -> 3.0 * PI / 2.0 // points upward (270 deg)
                }

                val startAngleRad = baseAngleRad - (arcSpanRad / 2.0)
                val stepRad = arcSpanRad / (count - 1).coerceAtLeast(1)

                actions.forEachIndexed { index, action ->
                    val angle = startAngleRad + index * stepRad
                    val itemOffsetX = (radius * cos(angle)).toFloat()
                    val itemOffsetY = (radius * sin(angle)).toFloat()

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(tween(160 + index * 20)) + scaleIn(tween(160 + index * 20)),
                        exit = fadeOut(tween(120)) + scaleOut(tween(120)),
                        modifier = Modifier.offset {
                            IntOffset(itemOffsetX.roundToInt(), itemOffsetY.roundToInt())
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { action.onClick() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(action.gradientColors))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                                    .shadow(8.dp, CircleShape)
                                    .testTag("dock_action_${action.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
