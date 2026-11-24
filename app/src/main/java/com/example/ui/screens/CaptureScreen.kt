package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.CognitiveDepthMode
import com.example.ai.CognitiveFormatArchetype
import com.example.audio.DetectedLanguage
import com.example.ui.components.AuditoryStatusChip
import com.example.ui.components.NeuralSoundwaveVisualizer
import com.example.ui.components.NeuroScribeLogo
import com.example.ui.components.PipelineStage
import com.example.ui.components.ProcessingPipelineHud
import com.example.ui.components.SpreadedAnimatedBackground
import com.example.ui.components.WorkflowBadge
import com.example.ui.components.cleanAsterisks
import com.example.ui.components.formatAudioDuration
import com.example.ui.theme.RetentionGold
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkflowMode
import com.example.util.ExportManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

@Composable
fun CaptureScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRecording by viewModel.recorderManager.isRecording.collectAsState()
    val amplitude by viewModel.recorderManager.amplitude.collectAsState()
    val decibels by viewModel.recorderManager.decibels.collectAsState()
    val durationMs by viewModel.recorderManager.recordingDurationMs.collectAsState()
    val partialText by viewModel.speechManager.partialTranscript.collectAsState()
    val finalText by viewModel.speechManager.finalTranscript.collectAsState()
    val auditoryFilterState by viewModel.auditoryFilterState.collectAsState()
    val workflowMode by viewModel.workflowMode.collectAsState()
    val cognitiveDepth by viewModel.cognitiveDepthMode.collectAsState()
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val selectedArchetype by viewModel.selectedFormatArchetype.collectAsState()
    val customPromptInstructions by viewModel.customPromptInstructions.collectAsState()
    val pipelineStage by viewModel.pipelineStage.collectAsState()
    val pipelineDetail by viewModel.pipelineDetail.collectAsState()
    val lastRecordingResult by viewModel.lastSavedResult.collectAsState()
    val latestSavedNote by viewModel.latestSavedNote.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val noSpeechState by viewModel.noSpeechDetectedState.collectAsState()

    // Player state for instant audio preview
    val isPlaying by viewModel.playerManager.isPlaying.collectAsState()
    val currentPositionMs by viewModel.playerManager.currentPositionMs.collectAsState()
    val totalDurationMs by viewModel.playerManager.totalDurationMs.collectAsState()

    var showLangMenu by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showManualNoteDialog by remember { mutableStateOf(false) }
    var showEditTranscriptDialog by remember { mutableStateOf(false) }
    var customPromptDraft by remember(customPromptInstructions) { mutableStateOf(customPromptInstructions) }

    // Dialog state
    var importTitle by remember { mutableStateOf("") }
    var importContent by remember { mutableStateOf("") }
    var importWithAi by remember { mutableStateOf(true) }

    var manualTitle by remember { mutableStateOf("") }
    var manualContent by remember { mutableStateOf("") }
    var manualCategory by remember { mutableStateOf("Work") }

    var manualSpokenInput by remember { mutableStateOf("") }

    val activeTranscript = finalText.ifBlank { partialText }.trim()
    var editableTranscriptText by remember(activeTranscript) { mutableStateOf(activeTranscript) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Pulse animation for recording
    val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Permission launcher for Recording and Foreground Notification Chamber
    val requiredPermissions = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
    }

    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val recordGranted = perms[Manifest.permission.RECORD_AUDIO] ?: false
        hasRecordPermission = recordGranted
        if (recordGranted) {
            viewModel.startListeningAndRecording()
        } else {
            Toast.makeText(context, "Microphone permission is required to capture speech", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio file picker (.m4a, .mp3, .wav, .aac, .ogg)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importAudioFile(uri, context)
        }
    }

    // Document file picker (.txt, .md)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val fileText = reader.readText()
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "imported_document.txt"

                    importTitle = fileName.substringBeforeLast('.')
                        .replace('_', ' ')
                        .replace('-', ' ')
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    importContent = fileText
                    showImportDialog = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not read file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera and Visual Document Ingestion Launchers
    var tempCameraFile by remember { mutableStateOf<java.io.File?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    viewModel.processCapturedImage(file, "image/jpeg")
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (file, uri) = viewModel.createTempCameraImageFile(context)
            tempCameraFile = file
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission is required to photograph notes", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importImageFile(uri, context)
        }
    }

    // Automatic Camera Trigger from Floating Dock / External Overlay
    val pendingCameraTrigger by viewModel.pendingCameraTrigger.collectAsState()
    androidx.compose.runtime.LaunchedEffect(pendingCameraTrigger) {
        if (pendingCameraTrigger) {
            val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (hasCam) {
                val (file, uri) = viewModel.createTempCameraImageFile(context)
                tempCameraFile = file
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            viewModel.clearCameraTrigger()
        }
    }

    SpreadedAnimatedBackground(
        modifier = modifier.fillMaxSize(),
        isDarkTheme = isDarkTheme
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Header Bar with Crisp Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Official Brand Logo
                NeuroScribeLogo(badgeSize = 38.dp, showSubtitle = true)

                // Professional Executive Toolbar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // New Session / Reset
                    IconButton(
                        onClick = {
                            viewModel.resetCaptureSession()
                            Toast.makeText(context, "Session ready for new recording.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(40.dp).testTag("action_refresh_session")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Session / Refresh",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Theme Switcher
                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.size(40.dp).testTag("action_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Light Mode" else "Dark Mode",
                            tint = if (isDarkTheme) RetentionGold else secondaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Language Selector
                    Box {
                        IconButton(
                            onClick = { showLangMenu = true },
                            modifier = Modifier.size(40.dp).testTag("action_lang_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            DetectedLanguage.entries.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text("${lang.flagEmoji} ${lang.label} (${lang.nativeLabel})", fontWeight = FontWeight.Medium, fontSize = 13.5.sp) },
                                    onClick = {
                                        viewModel.setSelectedLanguage(lang)
                                        showLangMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Floating Side Dock Quick Toggle
                    val isSideDockEnabled by viewModel.isSideDockEnabled.collectAsState()
                    IconButton(
                        onClick = {
                            viewModel.toggleSideDock()
                            Toast.makeText(context, if (!isSideDockEnabled) "Side Dock Enabled" else "Side Dock Hidden", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(40.dp).testTag("action_dock_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Toggle Side Dock",
                            tint = if (isSideDockEnabled) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Soundwave Visualizer & Telemetry HUD Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.4.dp,
                    primaryColor.copy(alpha = 0.55f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Balanced Telemetry Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AuditoryStatusChip(
                            filterState = auditoryFilterState,
                            detectedLang = selectedLang.label
                        )

                        Text(
                            text = if (isRecording) "44.1kHz • ${decibels.toInt()} dB" else "44.1kHz • STANDBY",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Enlarged 150dp multi-colored neural wave & spectrum equalizer
                    NeuralSoundwaveVisualizer(
                        amplitude = amplitude,
                        isRecording = isRecording
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Timer Display with High Contrast
                    Text(
                        text = if (isRecording) formatDuration(durationMs) else "00:00",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp,
                        color = if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Mode Architecture Selector: Combo (Audio+Mind) vs Cognitive Mind vs Raw Verbatim
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isCombo = (workflowMode == WorkflowMode.WORKFLOW_3_COMBO_MASTERWORK)
                val isMind = (workflowMode == WorkflowMode.WORKFLOW_2_MIND_SYNTHESIS)
                val isRaw = (workflowMode == WorkflowMode.WORKFLOW_1_RAW_STREAM)

                // Mode 1: Combo Archive (Selected by default)
                Box(
                    modifier = Modifier
                        .weight(1.15f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCombo) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .then(
                            if (isCombo) Modifier.border(1.2.dp, RetentionGold, RoundedCornerShape(12.dp))
                            else Modifier
                        )
                        .clickable { viewModel.setWorkflowMode(WorkflowMode.WORKFLOW_3_COMBO_MASTERWORK) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isCombo) RetentionGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "COMBO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isCombo) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isCombo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                // Mode 2: Cognitive Mind
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isMind) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .then(
                            if (isMind) Modifier.border(1.2.dp, secondaryColor, RoundedCornerShape(12.dp))
                            else Modifier
                        )
                        .clickable { viewModel.setWorkflowMode(WorkflowMode.WORKFLOW_2_MIND_SYNTHESIS) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (isMind) secondaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MIND",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isMind) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isMind) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                // Mode 3: Raw Audio Note
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isRaw) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .then(
                            if (isRaw) Modifier.border(1.2.dp, primaryColor, RoundedCornerShape(12.dp))
                            else Modifier
                        )
                        .clickable { viewModel.setWorkflowMode(WorkflowMode.WORKFLOW_1_RAW_STREAM) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (isRaw) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RAW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isRaw) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isRaw) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Cognitive Depth Mode Selector (Comprehensive Masterwork vs Concise Executive)
            if (workflowMode != WorkflowMode.WORKFLOW_1_RAW_STREAM) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isMasterwork = (cognitiveDepth == CognitiveDepthMode.COMPREHENSIVE_MASTERWORK)
                    val isConcise = (cognitiveDepth == CognitiveDepthMode.CONCISE_EXECUTIVE)

                    // Comprehensive Masterwork Mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isMasterwork) secondaryColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (isMasterwork) secondaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setCognitiveDepthMode(CognitiveDepthMode.COMPREHENSIVE_MASTERWORK) }
                            .padding(vertical = 7.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = if (isMasterwork) secondaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Comprehensive Masterwork",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isMasterwork) FontWeight.Bold else FontWeight.Normal,
                                color = if (isMasterwork) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Concise Executive Mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isConcise) primaryColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (isConcise) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setCognitiveDepthMode(CognitiveDepthMode.CONCISE_EXECUTIVE) }
                            .padding(vertical = 7.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = if (isConcise) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Concise Executive",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isConcise) FontWeight.Bold else FontWeight.Normal,
                                color = if (isConcise) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progressive Multi-Step Lazy-Loading Pipeline HUD (5 Clear Stages)
            AnimatedVisibility(
                visible = pipelineStage != PipelineStage.IDLE,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                ProcessingPipelineHud(
                    currentStage = pipelineStage,
                    statusDetail = pipelineDetail,
                    fileName = lastRecordingResult?.fileName
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Voice Capture Hub: Pure Professional Icon Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isRecording) {
                    // Radiant Centered Record Mic Action Node
                    val buttonBrush = Brush.radialGradient(listOf(primaryColor, primaryColor.copy(alpha = 0.85f)))

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(buttonBrush)
                            .border(2.5.dp, primaryColor.copy(alpha = 0.6f), CircleShape)
                            .clickable {
                                if (!hasRecordPermission) {
                                    permissionLauncher.launch(requiredPermissions)
                                } else {
                                    viewModel.startListeningAndRecording()
                                }
                            }
                            .testTag("record_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Start Recording",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    // Active Recording Controls: Pure Icon Buttons (Pause/Resume + Stop)
                    // Control 1: Pause / Resume Button
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (isPaused) primaryColor else RetentionGold)
                            .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                            .clickable {
                                if (isPaused) {
                                    viewModel.resumeRecording()
                                    Toast.makeText(context, "Recording resumed", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.pauseRecording()
                                    Toast.makeText(context, "Recording paused", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("action_pause_resume_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume Recording" else "Pause Recording",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Control 2: Stop & Synthesize Button
                    val buttonBrush = Brush.radialGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B)))

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(buttonBrush)
                            .border(2.5.dp, Color.White.copy(alpha = 0.95f), CircleShape)
                            .clickable {
                                viewModel.stopListeningAndRecording()
                            }
                            .testTag("action_stop_recording_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Recording",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Minimal, High-Precision Status Indicator
            Text(
                text = if (isRecording) {
                    if (isPaused) {
                        "PAUSED • ${formatDuration(durationMs)}"
                    } else {
                        "RECORDING • ${formatDuration(durationMs)}"
                    }
                } else {
                    "READY • 44.1 kHz"
                },
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = if (isRecording && isPaused) RetentionGold else if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Executive Quick Intake Hub: Pure Professional Icons (No Text Clutter)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tool 1: Camera Scan
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(primaryColor.copy(alpha = 0.12f))
                            .clickable {
                                val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                if (hasCam) {
                                    val (file, uri) = viewModel.createTempCameraImageFile(context)
                                    tempCameraFile = file
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                            .testTag("action_camera_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera Scan", tint = primaryColor, modifier = Modifier.size(22.dp))
                    }

                    // Tool 2: Upload Photo
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(secondaryColor.copy(alpha = 0.12f))
                            .clickable {
                                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            .testTag("action_upload_photo_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = "Upload Photo", tint = secondaryColor, modifier = Modifier.size(22.dp))
                    }

                    // Tool 3: Import Audio
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(RetentionGold.copy(alpha = 0.12f))
                            .clickable {
                                audioPickerLauncher.launch("audio/*")
                            }
                            .testTag("action_import_audio_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AudioFile, contentDescription = "Import Audio", tint = RetentionGold, modifier = Modifier.size(22.dp))
                    }

                    // Tool 4: Import Document
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                filePickerLauncher.launch("text/*")
                            }
                            .testTag("action_import_doc_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Import Document", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    }

                    // Tool 5: Blank Note
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                showManualNoteDialog = true
                            }
                            .testTag("action_blank_note_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.NoteAdd, contentDescription = "Blank Note", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live / Captured Speech Stream Card (if transcript exists)
            if (activeTranscript.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TRANSCRIBED SPEECH STREAM",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Edit Transcript Button
                            IconButton(
                                onClick = {
                                    editableTranscriptText = activeTranscript
                                    showEditTranscriptDialog = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Transcript",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cleanAsterisks(activeTranscript),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // PROMINENT RECENT RECORDED AUDIO NOTE CARD (Directly accessible below recording)
            latestSavedNote?.let { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("latest_saved_recording_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Storage & Workflow Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SAVED TO MEMORY ARCHIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp
                                )
                            }

                            WorkflowBadge(workflowType = note.workflowType)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Title
                        Text(
                            text = cleanAsterisks(note.title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // File Path & Size
                        val sizeStr = if (lastRecordingResult != null) "${lastRecordingResult!!.fileSizeBytes / 1024} KB" else "Verified"
                        Text(
                            text = "📁 /audio_notes/${note.audioFilePath?.substringAfterLast('/') ?: "audio.m4a"} • $sizeStr",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Built-in Instant Audio Player
                        if (!note.audioFilePath.isNullOrBlank()) {
                            val durationSafe = if (totalDurationMs > 0) totalDurationMs else (note.audioDurationMs.coerceAtLeast(1L))
                            val progress = (currentPositionMs.toFloat() / durationSafe.toFloat()).coerceIn(0f, 1f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.playerManager.playOrPause(note.audioFilePath) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Slider(
                                        value = progress,
                                        onValueChange = { viewModel.playerManager.seekTo((it * durationSafe).toLong()) },
                                        colors = SliderDefaults.colors(
                                            thumbColor = primaryColor,
                                            activeTrackColor = primaryColor
                                        ),
                                        modifier = Modifier.height(20.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatDuration(currentPositionMs),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatDuration(durationSafe),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Quick share audio button
                                IconButton(
                                    onClick = { ExportManager.shareAudioFile(context, note.audioFilePath, note.title) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Audio",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Open Document Action Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(primaryColor)
                                .clickable { viewModel.selectNote(note) }
                                .padding(vertical = 11.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OPEN CONVERTED DOCUMENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Dialog for Spoken Words Input (shown when no voice was detected in audio stream)
        noSpeechState?.let { info ->
            AlertDialog(
                onDismissRequest = { viewModel.clearNoSpeechDetected() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Transcribe Spoken Voice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = if (info.isImported) {
                                "The imported audio file was ingested. Enter the spoken text (Urdu, Roman Urdu, or English) to synthesize the structured document:"
                            } else {
                                "The microphone audio was recorded. If speech was silent or restricted in browser emulator, type what was spoken:"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = manualSpokenInput,
                            onValueChange = { manualSpokenInput = it },
                            label = { Text("Spoken Speech Text") },
                            placeholder = { Text("Enter words in Urdu, Roman Urdu, or English...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.clearNoSpeechDetected()
                                audioPickerLauncher.launch("audio/*")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Or Pick Another Audio File")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualSpokenInput.isNotBlank()) {
                                val text = manualSpokenInput
                                manualSpokenInput = ""
                                viewModel.clearNoSpeechDetected()
                                viewModel.processAndSaveCurrentCapture(
                                    initialTranscript = text,
                                    audioPath = info.audioPath,
                                    audioDurationMs = info.audioDurationMs,
                                    recordingResult = info.recordingResult
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Synthesize Document")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.clearNoSpeechDetected() }) {
                        Text("Dismiss")
                    }
                }
            )
        }

        // Edit Transcript Dialog
        if (showEditTranscriptDialog) {
            AlertDialog(
                onDismissRequest = { showEditTranscriptDialog = false },
                title = { Text("Edit Transcribed Words") },
                text = {
                    Column {
                        Text(
                            text = "Modify or type any spoken words in Urdu, Roman Urdu, or English:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editableTranscriptText,
                            onValueChange = { editableTranscriptText = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.speechManager.setTranscript(editableTranscriptText)
                            viewModel.processAndSaveCurrentCapture(
                                initialTranscript = editableTranscriptText,
                                audioPath = lastRecordingResult?.filePath,
                                audioDurationMs = lastRecordingResult?.durationMs ?: 2000L,
                                recordingResult = lastRecordingResult
                            )
                            showEditTranscriptDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Save & Synthesize")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditTranscriptDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Direct Document Import / Paste Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Import Document / Text") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = importTitle,
                            onValueChange = { importTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = importContent,
                            onValueChange = { importContent = it },
                            label = { Text("Document Content") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 8
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Switch(
                                checked = importWithAi,
                                onCheckedChange = { importWithAi = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Refine with Cognitive Mind",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (importContent.isNotBlank()) {
                                viewModel.importDocumentText(
                                    title = importTitle,
                                    rawText = importContent,
                                    refineWithAi = importWithAi
                                )
                                showImportDialog = false
                                importTitle = ""
                                importContent = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Process Document")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Manual Note Dialog
        if (showManualNoteDialog) {
            AlertDialog(
                onDismissRequest = { showManualNoteDialog = false },
                title = { Text("Create Blank Document") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = manualTitle,
                            onValueChange = { manualTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualCategory,
                            onValueChange = { manualCategory = it },
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualContent,
                            onValueChange = { manualContent = it },
                            label = { Text("Notes & Thoughts") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 8
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualTitle.isNotBlank() || manualContent.isNotBlank()) {
                                viewModel.createManualNote(
                                    title = manualTitle,
                                    content = manualContent,
                                    category = manualCategory
                                )
                                showManualNoteDialog = false
                                manualTitle = ""
                                manualContent = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualNoteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    return formatAudioDuration(millis)
}
