package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ShortText
import com.example.audio.DetectedLanguage
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.CognitiveDepthMode
import com.example.ai.CognitiveFormatArchetype
import com.example.ai.RefinementType
import com.example.data.model.NoteEntity
import com.example.ui.components.AudioPlayerCard
import com.example.ui.components.CompactAudioPlayerBar
import com.example.ui.components.CreativeLazyLoader
import com.example.ui.components.RichDocumentRenderer
import com.example.ui.components.SpreadedAnimatedBackground
import com.example.ui.components.WorkflowBadge
import com.example.ui.components.cleanAsterisks
import com.example.ui.theme.MindViolet
import com.example.ui.theme.RetentionGold
import com.example.ui.viewmodel.AiProcessingState
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.ExportManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    viewModel: MainViewModel,
    note: NoteEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    // Audio Player State
    val isPlaying by viewModel.playerManager.isPlaying.collectAsState()
    val currentPositionMs by viewModel.playerManager.currentPositionMs.collectAsState()
    val totalDurationMs by viewModel.playerManager.totalDurationMs.collectAsState()
    val playbackSpeed by viewModel.playerManager.playbackSpeed.collectAsState()

    val aiState by viewModel.aiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Document, 1: Raw Voice, 2: Glossary, 3: Flashcards
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showExportModal by remember { mutableStateOf(false) }
    var showTranslateMenu by remember { mutableStateOf(false) }
    var showFormatCustomDialog by remember { mutableStateOf(false) }
    var selectedFormatArchetype by remember { mutableStateOf(CognitiveFormatArchetype.RESEARCH_DISSERTATION) }
    var customReformatPrompt by remember { mutableStateOf("") }

    // Edit form state
    var editTitle by remember(note.title) { mutableStateOf(note.title) }
    var editContent by remember(note.synthesizedContent) { mutableStateOf(note.synthesizedContent) }
    var editTags by remember(note.tags) { mutableStateOf(note.tags) }
    var editCategory by remember(note.category) { mutableStateOf(note.category) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Back handling
    BackHandler {
        viewModel.playerManager.stop()
        viewModel.navigateTo(AppScreen.ARCHIVE)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DOCUMENT VIEWER",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.playerManager.stop()
                            viewModel.navigateTo(AppScreen.ARCHIVE)
                        },
                        modifier = Modifier.testTag("note_detail_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Theme Switcher
                    IconButton(onClick = { viewModel.toggleDarkTheme() }) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkTheme) RetentionGold else secondaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Memory Tier Toggle (Short term vs Long term)
                    IconButton(
                        onClick = { viewModel.toggleMemoryTier(note) },
                        modifier = Modifier.testTag("toggle_memory_tier_button")
                    ) {
                        Icon(
                            imageVector = if (note.isLongTermMemory) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (note.isLongTermMemory) "Long-Term Memory" else "Sensory Buffer",
                            tint = if (note.isLongTermMemory) RetentionGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Delete Note Action
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("delete_note_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        SpreadedAnimatedBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            isDarkTheme = isDarkTheme
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
            // Concise Note Meta & Compact Audio Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WorkflowBadge(workflowType = note.workflowType)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${note.language.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (note.isLongTermMemory) "RETAINED (${note.retentionScore}%)" else "SENSORY BUFFER",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = if (note.isLongTermMemory) RetentionGold else primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = cleanAsterisks(note.title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )

                    // Compact audio bar (concise height)
                    if (!note.audioFilePath.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        CompactAudioPlayerBar(
                            audioFilePath = note.audioFilePath,
                            isPlaying = isPlaying,
                            currentPositionMs = currentPositionMs,
                            totalDurationMs = if (totalDurationMs > 0) totalDurationMs else note.audioDurationMs,
                            playbackSpeed = playbackSpeed,
                            onPlayPause = { viewModel.playerManager.playOrPause(note.audioFilePath) },
                            onSeek = { viewModel.playerManager.seekTo(it) },
                            onSpeedChange = { viewModel.playerManager.setSpeed(it) }
                        )
                    }
                }
            }

            // AI Processing Status Indicator & Creative Lazy Loader
            if (aiState is AiProcessingState.Processing) {
                CreativeLazyLoader(
                    message = (aiState as AiProcessingState.Processing).stageMessage,
                    subMessage = "Structuring Research • Verifying Conceptual Precision"
                )
            }

            // Tabs with Icons: 0: Document, 1: Raw Voice, 2: Glossary, 3: Flashcards
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = primaryColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = primaryColor
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    text = { Text("DOCUMENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    text = { Text("TRANSCRIPT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    text = { Text("KEY TERMS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    text = { Text("RETENTION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                )
            }

            // Dedicated Executive Knowledge Actions Bar (8 Unique Non-Duplicate Functional Icons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Dynamic Format & Custom Detail Specification
                IconButton(
                    onClick = { showFormatCustomDialog = true },
                    modifier = Modifier.size(38.dp).testTag("action_format_custom")
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = "Format & Details", tint = secondaryColor, modifier = Modifier.size(20.dp))
                }

                // 2. Polish Academic Tone
                IconButton(
                    onClick = {
                        viewModel.refineSelectedNote(RefinementType.POLISH_STYLE)
                        Toast.makeText(context, "Polishing academic tone...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(38.dp).testTag("action_ai_polish")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Polish Academic Tone", tint = primaryColor, modifier = Modifier.size(20.dp))
                }

                // 3. Translate Language (All 16 Global Languages)
                IconButton(
                    onClick = { showTranslateMenu = true },
                    modifier = Modifier.size(38.dp).testTag("action_ai_translate")
                ) {
                    Icon(imageVector = Icons.Default.Translate, contentDescription = "Translate All Languages", tint = secondaryColor, modifier = Modifier.size(20.dp))
                }

                // 4. Simplify Core Axioms (Feynman ELI5)
                IconButton(
                    onClick = {
                        viewModel.refineSelectedNote(RefinementType.SIMPLIFY_ELI5)
                        Toast.makeText(context, "Simplifying core axioms (ELI5)...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(38.dp).testTag("action_ai_simplify")
                ) {
                    Icon(imageVector = Icons.Default.School, contentDescription = "Simplify (ELI5)", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                }

                // 5. Action Checklist Extraction
                IconButton(
                    onClick = {
                        viewModel.refineSelectedNote(RefinementType.GENERATE_CHECKLIST)
                        Toast.makeText(context, "Extracting action checklist...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(38.dp).testTag("action_ai_checklist")
                ) {
                    Icon(imageVector = Icons.Default.ListAlt, contentDescription = "Action Checklist", tint = RetentionGold, modifier = Modifier.size(20.dp))
                }

                // 6. Edit Document Content
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.size(38.dp).testTag("action_edit_note")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Note", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                }

                // 7. Export & Share Sheet
                IconButton(
                    onClick = { showExportModal = true },
                    modifier = Modifier.size(38.dp).testTag("action_share_note")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Export & Share", tint = primaryColor, modifier = Modifier.size(20.dp))
                }

                // 8. One-Tap Times New Roman PDF Export
                IconButton(
                    onClick = {
                        ExportManager.exportNoteToPdf(context, note.title, note.synthesizedContent)
                    },
                    modifier = Modifier.size(38.dp).testTag("action_export_pdf")
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                }
            }

            // Tab Content - Generous full-height reading viewport
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                when (selectedTab) {
                    0 -> DocumentNotepadView(
                        content = note.synthesizedContent,
                        noteTitle = note.title,
                        imageFilePath = note.imageFilePath,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(cleanAsterisks(note.synthesizedContent)))
                            Toast.makeText(context, "Clean document copied", Toast.LENGTH_SHORT).show()
                        },
                        onToggleDepth = { mode ->
                            if (mode == CognitiveDepthMode.COMPREHENSIVE_MASTERWORK) {
                                viewModel.refineSelectedNote(RefinementType.EXPAND_COMPREHENSIVE_MASTERWORK)
                            } else {
                                viewModel.refineSelectedNote(RefinementType.CONVERT_TO_CONCISE)
                            }
                        }
                    )
                    1 -> RawVoiceTranscriptView(transcript = note.rawTranscript, onCopy = {
                        clipboardManager.setText(AnnotatedString(cleanAsterisks(note.rawTranscript)))
                        Toast.makeText(context, "Raw transcript copied", Toast.LENGTH_SHORT).show()
                    })
                    2 -> GlossaryView(keyTerms = note.keyTerms, actionItems = note.actionItems)
                    3 -> RetentionPromptsView(viewModel = viewModel, note = note)
                }
            }
        }

        // 16-Language Global Real-Time Translation Dialog
        if (showTranslateMenu) {
            val allTranslationTargets = listOf(
                Triple(DetectedLanguage.ENGLISH, "English (Global Academic)", RefinementType.TRANSLATE_ENGLISH),
                Triple(DetectedLanguage.URDU, "اردو (Jameel Noori Nastaleeq)", RefinementType.TRANSLATE_URDU),
                Triple(DetectedLanguage.ROMAN_URDU, "Roman Urdu (Latin Script)", RefinementType.TRANSLATE_ROMAN_URDU),
                Triple(DetectedLanguage.ARABIC, "العربية الفصحى (Modern Arabic)", RefinementType.TRANSLATE_ARABIC),
                Triple(DetectedLanguage.SPANISH, "Español (Castilian Spanish)", RefinementType.TRANSLATE_SPANISH),
                Triple(DetectedLanguage.FRENCH, "Français (Academic French)", RefinementType.TRANSLATE_FRENCH),
                Triple(DetectedLanguage.GERMAN, "Deutsch (Technical German)", RefinementType.TRANSLATE_GERMAN),
                Triple(DetectedLanguage.CHINESE, "中文 (Simplified Chinese)", RefinementType.TRANSLATE_CHINESE),
                Triple(DetectedLanguage.JAPANESE, "日本語 (Standard Japanese)", RefinementType.TRANSLATE_JAPANESE),
                Triple(DetectedLanguage.HINDI, "हिन्दी (Standard Hindi)", RefinementType.TRANSLATE_HINDI),
                Triple(DetectedLanguage.RUSSIAN, "Русский (Academic Russian)", RefinementType.TRANSLATE_RUSSIAN),
                Triple(DetectedLanguage.PORTUGUESE, "Português (Portuguese)", RefinementType.TRANSLATE_PORTUGUESE),
                Triple(DetectedLanguage.TURKISH, "Türkçe (Standard Turkish)", RefinementType.TRANSLATE_TURKISH),
                Triple(DetectedLanguage.PERSIAN, "فارسی (Persian / Farsi)", RefinementType.TRANSLATE_PERSIAN),
                Triple(DetectedLanguage.ITALIAN, "Italiano (Refined Italian)", RefinementType.TRANSLATE_ITALIAN),
                Triple(DetectedLanguage.KOREAN, "한국어 (Standard Korean)", RefinementType.TRANSLATE_KOREAN)
            )

            AlertDialog(
                onDismissRequest = { showTranslateMenu = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = secondaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Global Real-Time Translation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Select any global language to translate the synthesized document in real time:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        allTranslationTargets.forEach { (lang, label, refinementType) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showTranslateMenu = false
                                        viewModel.refineSelectedNote(refinementType)
                                        Toast.makeText(context, "Translating into ${lang.label}...", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = lang.flagEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(text = lang.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                        }
                                    }
                                    Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showTranslateMenu = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Custom Format & Requested Details Specification Dialog
        if (showFormatCustomDialog) {
            AlertDialog(
                onDismissRequest = { showFormatCustomDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = secondaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Re-Synthesize Document Format", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Beautiful Pure Icon Archetype Gallery (all 11 archetypes)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CognitiveFormatArchetype.entries.forEach { archetype ->
                                ArchetypeIconBadge(
                                    archetype = archetype,
                                    isSelected = selectedFormatArchetype == archetype,
                                    onClick = { selectedFormatArchetype = archetype }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customReformatPrompt,
                            onValueChange = { customReformatPrompt = it },
                            placeholder = { Text("Custom details specification (optional)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.reformatSelectedNote(selectedFormatArchetype, customReformatPrompt)
                            showFormatCustomDialog = false
                            Toast.makeText(context, "Re-synthesizing into ${selectedFormatArchetype.title}...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryColor,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("Re-Synthesize")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFormatCustomDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Export & Share Modal Dialog
        if (showExportModal) {
            AlertDialog(
                onDismissRequest = { showExportModal = false },
                title = { Text("Export & Share Document", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // PDF Print Export
                        ExportOptionTile(
                            icon = Icons.Default.PictureAsPdf,
                            title = "Export / Print as PDF",
                            subtitle = "Formatted document ready for save or printing",
                            color = Color(0xFFEF4444)
                        ) {
                            showExportModal = false
                            ExportManager.exportToPdfPrint(context, note.title, note.synthesizedContent)
                        }

                        // Text Document Export
                        ExportOptionTile(
                            icon = Icons.Default.Description,
                            title = "Export as Text Document (.txt)",
                            subtitle = "Clean text document without asterisks",
                            color = primaryColor
                        ) {
                            showExportModal = false
                            ExportManager.exportAndShareFile(context, note.title, note.synthesizedContent, isMarkdown = false)
                        }

                        // Markdown Export
                        ExportOptionTile(
                            icon = Icons.Default.Article,
                            title = "Export as Markdown (.md)",
                            subtitle = "Standard markdown file for notes apps",
                            color = secondaryColor
                        ) {
                            showExportModal = false
                            ExportManager.exportAndShareFile(context, note.title, note.synthesizedContent, isMarkdown = true)
                        }

                        // Share Audio Recording (if present)
                        if (!note.audioFilePath.isNullOrBlank()) {
                            ExportOptionTile(
                                icon = Icons.Default.AudioFile,
                                title = "Share Audio Recording (.m4a)",
                                subtitle = "Send original audio voice recording file",
                                color = RetentionGold
                            ) {
                                showExportModal = false
                                ExportManager.shareAudioFile(context, note.audioFilePath, note.title)
                            }
                        }

                        // Plain Text Share Sheet
                        ExportOptionTile(
                            icon = Icons.Default.Share,
                            title = "Share via Apps",
                            subtitle = "Send via WhatsApp, Gmail, Messages, etc.",
                            color = Color(0xFF10B981)
                        ) {
                            showExportModal = false
                            ExportManager.sharePlainText(context, note.title, note.synthesizedContent)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showExportModal = false }) { Text("Close") }
                }
            )
        }

        // Manual Edit Note Dialog
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Document", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editCategory,
                            onValueChange = { editCategory = it },
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editContent,
                            onValueChange = { editContent = it },
                            label = { Text("Document Content") },
                            minLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateNoteContent(editTitle, editContent, editTags, editCategory)
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Save Changes", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Delete Confirm Dialog
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete Note?") },
                text = { Text("This will remove this document and its associated audio file from your archive.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.playerManager.stop()
                            viewModel.deleteNote(note)
                            showDeleteConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}
}

@Composable
private fun ExportOptionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun DocumentNotepadView(
    content: String,
    noteTitle: String = "",
    imageFilePath: String? = null,
    onCopy: () -> Unit,
    onToggleDepth: (CognitiveDepthMode) -> Unit
) {
    val scrollState = rememberScrollState()
    var isAutoReading by remember { mutableStateOf(false) }
    var readingSpeed by remember { mutableFloatStateOf(1.0f) }
    var isFullScreenReading by remember { mutableStateOf(false) }
    var fontZoomScale by remember { mutableFloatStateOf(1.0f) }

    // Auto fluctuating reading scroll effect
    LaunchedEffect(isAutoReading, readingSpeed) {
        if (isAutoReading) {
            while (isActive && isAutoReading) {
                val max = scrollState.maxValue
                if (max > 0 && scrollState.value >= max) {
                    // Complete reading! Automatically and smoothly returns to standard view
                    isAutoReading = false
                    isFullScreenReading = false
                    break
                }
                val step = (2.2f * readingSpeed).roundToInt().coerceAtLeast(1)
                scrollState.scrollBy(step.toFloat())
                delay(32) // ~30 fps smooth reading pace
            }
        }
    }

    // 100% Fullscreen Immersive Book Reading View
    if (isFullScreenReading) {
        BackHandler {
            isFullScreenReading = false
            isAutoReading = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("fullscreen_reading_view")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Top Immersive Reader Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "IMMERSIVE BOOK READER",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            val progressPct = if (scrollState.maxValue > 0) {
                                ((scrollState.value.toFloat() / scrollState.maxValue.toFloat()) * 100).toInt().coerceIn(0, 100)
                            } else 0
                            Text(
                                text = "$progressPct% read • ${readingSpeed}x teleprompter",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Font Zoom Out
                        IconButton(
                            onClick = { fontZoomScale = (fontZoomScale - 0.1f).coerceAtLeast(0.85f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("A-", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        // Font Zoom In
                        IconButton(
                            onClick = { fontZoomScale = (fontZoomScale + 0.1f).coerceAtMost(1.35f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("A+", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        // Exit Fullscreen
                        IconButton(
                            onClick = {
                                isFullScreenReading = false
                                isAutoReading = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Fullscreen",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // 100% Reading Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 85.dp)
                    ) {
                        RichDocumentRenderer(content = content, noteTitle = noteTitle)
                    }

                    // Floating Glass Teleprompter Controller Bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                            .border(1.5.dp, Color(0xFF10B981).copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Play / Pause
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                                    .clickable { isAutoReading = !isAutoReading },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAutoReading) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            val progressPct = if (scrollState.maxValue > 0) {
                                ((scrollState.value.toFloat() / scrollState.maxValue.toFloat()) * 100).toInt().coerceIn(0, 100)
                            } else 0
                            Text(
                                text = "$progressPct%",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )

                            // Speed Presets
                            listOf(0.75f, 1.0f, 1.5f, 2.0f).forEach { spd ->
                                val isSel = (readingSpeed == spd)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) Color(0xFF10B981) else Color.White.copy(alpha = 0.15f))
                                        .clickable { readingSpeed = spd }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${spd}x",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color.White.copy(alpha = 0.75f)
                                    )
                                }
                            }

                            // Dismiss button
                            IconButton(
                                onClick = {
                                    isFullScreenReading = false
                                    isAutoReading = false
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Exit Reading Mode",
                                    tint = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Standard Viewport Document View (~30-40% below header and metadata)
        Box(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Clean Document Icon Badge (no redundant text)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Right: Professional Action Icons with generous white spacing
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Single Unified Immersive Mode Action
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        isFullScreenReading = true
                                        isAutoReading = true
                                    }
                                    .testTag("btn_immersive_mode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = "Immersive Mode",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Quick Depth: Deep Study Mode
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f))
                                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                    .clickable { onToggleDepth(CognitiveDepthMode.COMPREHENSIVE_MASTERWORK) }
                                    .testTag("btn_deep_study"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Deep Study Mode",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Quick Depth: Concise Executive Mode
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                    .clickable { onToggleDepth(CognitiveDepthMode.CONCISE_EXECUTIVE) }
                                    .testTag("btn_concise_mode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShortText,
                                    contentDescription = "Concise Executive Mode",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Copy Document
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .clickable(onClick = onCopy)
                                    .testTag("btn_copy_document"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Document",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )

                    // Display captured photo/image if present
                    if (!imageFilePath.isNullOrBlank()) {
                        val imageFile = java.io.File(imageFilePath)
                        if (imageFile.exists()) {
                            coil.compose.AsyncImage(
                                model = imageFile,
                                contentDescription = "Ingested Document Visual",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        RichDocumentRenderer(content = content, noteTitle = noteTitle)
                    }
                }
            }
        }
    }
}

@Composable
private fun RawVoiceTranscriptView(transcript: String, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VERBATIM TRANSCRIPT",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Transcript", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = cleanAsterisks(transcript).ifBlank { "No verbatim transcript recorded." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 23.sp
                )
            }
        }
    }
}

@Composable
private fun GlossaryView(keyTerms: String, actionItems: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "KEY TERMS & CONCEPTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (keyTerms.isNotBlank()) {
                    RichDocumentRenderer(content = keyTerms)
                } else {
                    Text(text = "No technical terms highlighted.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "ACTION CHECKLIST",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = RetentionGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (actionItems.isNotBlank()) {
                    RichDocumentRenderer(content = actionItems)
                } else {
                    Text(text = "No action items extracted.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RetentionPromptsView(viewModel: MainViewModel, note: NoteEntity) {
    val allCards by viewModel.allFlashcards.collectAsState()
    val noteCards = remember(allCards, note.id) { allCards.filter { it.noteId == note.id } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Action Bar for Assessment & Grounded Q&A
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.generateQuizForNote(note) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("action_note_generate_quiz"),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Quiz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    viewModel.setSearchQuery(note.title)
                    viewModel.navigateTo(AppScreen.ASK_AI)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("action_note_ask_ai"),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ask AI", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (noteCards.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "No Socratic Flashcards Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Generate Quiz' above or use AI Refine to build spaced recall questions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            noteCards.forEach { card ->
                var revealed by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { revealed = !revealed },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (revealed) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (revealed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "RECALL QUESTION",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                            if (card.isMastered) {
                                Text(text = "✓ Mastered", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = cleanAsterisks(card.question), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (revealed) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp)
                            ) {
                                Text(text = cleanAsterisks(card.answer), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        } else {
                            Text(text = "Tap to reveal answer...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchetypeIconBadge(
    archetype: CognitiveFormatArchetype,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (icon, color1, color2) = when (archetype) {
        CognitiveFormatArchetype.RESEARCH_DISSERTATION -> Triple(Icons.Default.School, Color(0xFF4F46E5), Color(0xFF6366F1))
        CognitiveFormatArchetype.IMPLEMENTATION_BLUEPRINT -> Triple(Icons.Default.Code, Color(0xFF0284C7), Color(0xFF06B6D4))
        CognitiveFormatArchetype.EXECUTIVE_DECISION_MATRIX -> Triple(Icons.Default.Analytics, Color(0xFFD97706), Color(0xFFF59E0B))
        CognitiveFormatArchetype.STEP_BY_STEP_PLAYBOOK -> Triple(Icons.Default.ListAlt, Color(0xFF059669), Color(0xFF10B981))
        CognitiveFormatArchetype.SOCRATIC_TUTORIAL_QA -> Triple(Icons.Default.QuestionAnswer, Color(0xFF7C3AED), Color(0xFF8B5CF6))
        CognitiveFormatArchetype.VISUAL_MINDMAP_OUTLINE -> Triple(Icons.Default.AccountTree, Color(0xFF2563EB), Color(0xFF3B82F6))
        CognitiveFormatArchetype.CLINICAL_SCIENTIFIC_DOSSIER -> Triple(Icons.Default.Psychology, Color(0xFFDC2626), Color(0xFFEF4444))
        CognitiveFormatArchetype.LEGAL_STATUTORY_ANALYSIS -> Triple(Icons.Default.FactCheck, Color(0xFF475569), Color(0xFF64748B))
        CognitiveFormatArchetype.PHILOSOPHICAL_FIRST_PRINCIPLES -> Triple(Icons.Default.Lightbulb, Color(0xFFCA8A04), Color(0xFFEAB308))
        CognitiveFormatArchetype.CREATIVE_NARRATIVE_ESSAY -> Triple(Icons.Default.MenuBook, Color(0xFFDB2777), Color(0xFFEC4899))
        CognitiveFormatArchetype.CUSTOM_USER_SPECIFICATION -> Triple(Icons.Default.Tune, Color(0xFF0D9488), Color(0xFF14B8A6))
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(color1, color2)))
            .border(
                if (isSelected) 2.5.dp else 1.dp,
                if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                CircleShape
            )
            .clickable(onClick = onClick)
            .testTag("archetype_badge_${archetype.name}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = archetype.title,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = color1,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun ArchetypeIconChip(
    archetype: CognitiveFormatArchetype,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val icon = when (archetype) {
        CognitiveFormatArchetype.RESEARCH_DISSERTATION -> Icons.Default.School
        CognitiveFormatArchetype.IMPLEMENTATION_BLUEPRINT -> Icons.Default.Code
        CognitiveFormatArchetype.EXECUTIVE_DECISION_MATRIX -> Icons.Default.Analytics
        CognitiveFormatArchetype.STEP_BY_STEP_PLAYBOOK -> Icons.Default.ListAlt
        CognitiveFormatArchetype.SOCRATIC_TUTORIAL_QA -> Icons.Default.QuestionAnswer
        CognitiveFormatArchetype.VISUAL_MINDMAP_OUTLINE -> Icons.Default.AccountTree
        CognitiveFormatArchetype.CLINICAL_SCIENTIFIC_DOSSIER -> Icons.Default.Psychology
        CognitiveFormatArchetype.LEGAL_STATUTORY_ANALYSIS -> Icons.Default.FactCheck
        CognitiveFormatArchetype.PHILOSOPHICAL_FIRST_PRINCIPLES -> Icons.Default.Lightbulb
        CognitiveFormatArchetype.CREATIVE_NARRATIVE_ESSAY -> Icons.Default.MenuBook
        CognitiveFormatArchetype.CUSTOM_USER_SPECIFICATION -> Icons.Default.Tune
    }
    val shortLabel = when (archetype) {
        CognitiveFormatArchetype.RESEARCH_DISSERTATION -> "Research"
        CognitiveFormatArchetype.IMPLEMENTATION_BLUEPRINT -> "Blueprint"
        CognitiveFormatArchetype.EXECUTIVE_DECISION_MATRIX -> "Executive"
        CognitiveFormatArchetype.STEP_BY_STEP_PLAYBOOK -> "Playbook"
        CognitiveFormatArchetype.SOCRATIC_TUTORIAL_QA -> "Socratic"
        CognitiveFormatArchetype.VISUAL_MINDMAP_OUTLINE -> "Mind Map"
        CognitiveFormatArchetype.CLINICAL_SCIENTIFIC_DOSSIER -> "Scientific"
        CognitiveFormatArchetype.LEGAL_STATUTORY_ANALYSIS -> "Legal"
        CognitiveFormatArchetype.PHILOSOPHICAL_FIRST_PRINCIPLES -> "First Princ."
        CognitiveFormatArchetype.CREATIVE_NARRATIVE_ESSAY -> "Narrative"
        CognitiveFormatArchetype.CUSTOM_USER_SPECIFICATION -> "Custom"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) secondaryColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.2.dp,
                if (isSelected) secondaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = shortLabel,
                tint = if (isSelected) secondaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = shortLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = 10.sp,
                color = if (isSelected) secondaryColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
