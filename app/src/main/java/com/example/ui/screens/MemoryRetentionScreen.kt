package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.StudyPlanEntity
import com.example.util.ExportManager
import com.example.util.SpacedRepetitionScheduler.ReviewGrade
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.FlashcardEntity
import com.example.data.model.NoteEntity
import com.example.ui.components.SpreadedAnimatedBackground
import com.example.ui.components.cleanAsterisks
import com.example.ui.theme.RetentionGold
import com.example.ui.viewmodel.MainViewModel

/**
 * Advanced Socratic Cognitive Mastery Lab & Retention Engine.
 * Replaces basic flashcards with:
 * 1. Interactive Socratic Active Recall Drill (one-by-one oral simulation with voice replay)
 * 2. Academic Knowledge Taxonomy Matrix (structured concept categorization)
 * 3. Ebbinghaus Forgetting Curve Telemetry & One-Tap Cognitive Consolidation
 */
@Composable
fun MemoryRetentionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allNotes by viewModel.allNotes.collectAsState()
    val allCards by viewModel.allFlashcards.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    // 0: Socratic Drill, 1: Taxonomy Matrix, 2: Decay & Continuum Deck
    var activeLabTab by remember { mutableIntStateOf(0) }

    val shortTermCount = allNotes.count { !it.isLongTermMemory }
    val longTermCount = allNotes.count { it.isLongTermMemory }
    val masteredCards = allCards.count { it.isMastered }

    val retentionHealth = if (allNotes.isNotEmpty()) {
        val noteFactor = (longTermCount.toFloat() / allNotes.size.toFloat()) * 60f
        val cardFactor = if (allCards.isNotEmpty()) (masteredCards.toFloat() / allCards.size.toFloat()) * 40f else 40f
        (noteFactor + cardFactor).toInt().coerceIn(15, 100)
    } else {
        100
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
            // Executive Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Retention & Recall",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${allCards.size} cards • $retentionHealth% retention rate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Export Retention Dossier Button
                    IconButton(
                        onClick = {
                            ExportManager.exportRetentionDossier(context, allCards, allNotes)
                            Toast.makeText(context, "Exporting Retention Dossier...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp).testTag("action_export_retention")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export Retention Dossier",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkTheme) RetentionGold else secondaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RetentionGold.copy(alpha = 0.15f))
                            .border(1.dp, RetentionGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = RetentionGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$retentionHealth%",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = RetentionGold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lab Segmented Navigation Tabs
            TabRow(
                selectedTabIndex = activeLabTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = primaryColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeLabTab]),
                        color = primaryColor
                    )
                }
            ) {
                Tab(
                    selected = activeLabTab == 0,
                    onClick = { activeLabTab = 0 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    text = { Text("SM-2 DRILL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp) }
                )
                Tab(
                    selected = activeLabTab == 1,
                    onClick = { activeLabTab = 1 },
                    icon = { Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    text = { Text("QUIZZES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp) }
                )
                Tab(
                    selected = activeLabTab == 2,
                    onClick = { activeLabTab = 2 },
                    icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    text = { Text("STUDY PLANS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp) }
                )
                Tab(
                    selected = activeLabTab == 3,
                    onClick = { activeLabTab = 3 },
                    icon = { Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    text = { Text("TAXONOMY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp) }
                )
                Tab(
                    selected = activeLabTab == 4,
                    onClick = { activeLabTab = 4 },
                    icon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    text = { Text("CONTINUUM", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeLabTab) {
                0 -> SocraticActiveDrillView(
                    cards = allCards,
                    notes = allNotes,
                    viewModel = viewModel
                )
                1 -> QuizzesAssessmentsLabView(
                    viewModel = viewModel,
                    notes = allNotes
                )
                2 -> StudyPlansLabView(
                    viewModel = viewModel
                )
                3 -> KnowledgeTaxonomyMatrixView(
                    cards = allCards,
                    notes = allNotes,
                    viewModel = viewModel
                )
                4 -> MemoryContinuumDeckView(
                    cards = allCards,
                    notes = allNotes,
                    retentionHealth = retentionHealth,
                    shortTermCount = shortTermCount,
                    longTermCount = longTermCount,
                    masteredCards = masteredCards,
                    viewModel = viewModel
                )
            }
        }
    }
}

/**
 * Tab 0: Interactive Socratic Oral Examination.
 * Presents one recall prompt at a time with confidence scoring, reveal animation,
 * and audio flashback to the original recording if available.
 */
@Composable
private fun SocraticActiveDrillView(
    cards: List<FlashcardEntity>,
    notes: List<NoteEntity>,
    viewModel: MainViewModel
) {
    val dueCards by viewModel.dueFlashcards.collectAsState()
    var showDueOnly by remember { mutableStateOf(false) }
    val effectiveCards = if (showDueOnly && dueCards.isNotEmpty()) dueCards else cards

    if (effectiveCards.isEmpty()) {
        EmptyRecallState()
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isAnswerRevealed by remember { mutableStateOf(false) }

    // Guard bounds
    val safeIndex = currentIndex.coerceIn(0, effectiveCards.size - 1)
    val currentCard = effectiveCards[safeIndex]
    val sourceNote = remember(currentCard.noteId, notes) {
        notes.firstOrNull { it.id == currentCard.noteId }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Due filter selector and SM-2 telemetry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = !showDueOnly,
                    onClick = {
                        showDueOnly = false
                        currentIndex = 0
                        isAnswerRevealed = false
                    },
                    label = { Text("All (${cards.size})", fontSize = 10.sp) }
                )
                FilterChip(
                    selected = showDueOnly,
                    onClick = {
                        showDueOnly = true
                        currentIndex = 0
                        isAnswerRevealed = false
                    },
                    label = { Text("Due Today (${dueCards.size})", fontSize = 10.sp) }
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(primaryColor.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "EF: ${String.format(java.util.Locale.US, "%.2f", currentCard.easeFactor)} • ${currentCard.intervalDays}d",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = primaryColor,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Top Navigation Ribbon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(secondaryColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = currentCard.conceptKey.ifBlank { "COGNITIVE AXIOM" }.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = secondaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }

                if (currentCard.isMastered) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✓ MASTERED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Text(
                text = "${safeIndex + 1} of ${effectiveCards.size}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Socratic Prompt Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOCRATIC QUESTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 10.sp
                        )

                        // Voice Flashback Pill if source note has audio
                        if (!sourceNote?.audioFilePath.isNullOrBlank()) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(primaryColor.copy(alpha = 0.12f))
                                    .clickable {
                                        viewModel.playerManager.playOrPause(sourceNote!!.audioFilePath!!)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AUDIO ANCHOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = cleanAsterisks(currentCard.question),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Revealed Answer & Socratic Analysis
                    if (isAnswerRevealed) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .border(1.dp, primaryColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "ANALYSIS & ANSWER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryColor,
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = cleanAsterisks(currentCard.answer),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 21.sp
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .clickable { isAnswerRevealed = true }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tap to Reveal Socratic Analysis",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                        }
                    }
                }

                // Bottom Self-Evaluation Scoring Hub (SuperMemo SM-2 Spaced Repetition)
                Column {
                    if (isAnswerRevealed) {
                        Text(
                            text = "SM-2 RECALL EVALUATION (INTERVAL PREDICTION)",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 8.5.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Again (Quality 1)
                            OutlinedButton(
                                onClick = {
                                    viewModel.submitSm2Review(currentCard, ReviewGrade.AGAIN)
                                    isAnswerRevealed = false
                                    if (currentIndex < effectiveCards.size - 1) currentIndex++ else currentIndex = 0
                                },
                                modifier = Modifier.weight(1f).testTag("sm2_again_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Again", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("< 1d", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                            }

                            // Hard (Quality 3)
                            OutlinedButton(
                                onClick = {
                                    viewModel.submitSm2Review(currentCard, ReviewGrade.HARD)
                                    isAnswerRevealed = false
                                    if (currentIndex < effectiveCards.size - 1) currentIndex++ else currentIndex = 0
                                },
                                modifier = Modifier.weight(1f).testTag("sm2_hard_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Hard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    val hardInterval = maxOf(1, (currentCard.intervalDays * 1.2).toInt())
                                    Text("${hardInterval}d", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                            }

                            // Good (Quality 4)
                            Button(
                                onClick = {
                                    viewModel.submitSm2Review(currentCard, ReviewGrade.GOOD)
                                    isAnswerRevealed = false
                                    if (currentIndex < effectiveCards.size - 1) currentIndex++ else currentIndex = 0
                                },
                                modifier = Modifier.weight(1f).testTag("sm2_good_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Good", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    val goodInterval = if (currentCard.repetitions == 0) 1 else if (currentCard.repetitions == 1) 6 else maxOf(1, (currentCard.intervalDays * currentCard.easeFactor).toInt())
                                    Text("${goodInterval}d", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.85f))
                                }
                            }

                            // Easy (Quality 5)
                            Button(
                                onClick = {
                                    viewModel.submitSm2Review(currentCard, ReviewGrade.EASY)
                                    isAnswerRevealed = false
                                    if (currentIndex < effectiveCards.size - 1) currentIndex++ else currentIndex = 0
                                },
                                modifier = Modifier.weight(1f).testTag("sm2_easy_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Easy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    val easyInterval = if (currentCard.repetitions == 0) 4 else maxOf(2, (currentCard.intervalDays * currentCard.easeFactor * 1.3).toInt())
                                    Text("${easyInterval}d", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.85f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Next / Prev Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentIndex > 0) {
                                    currentIndex--
                                    isAnswerRevealed = false
                                }
                            },
                            enabled = safeIndex > 0
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Prompt")
                        }

                        LinearProgressIndicator(
                            progress = { ((safeIndex + 1).toFloat() / effectiveCards.size.toFloat()) },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = primaryColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        IconButton(
                            onClick = {
                                if (safeIndex < effectiveCards.size - 1) {
                                    currentIndex++
                                    isAnswerRevealed = false
                                }
                            },
                            enabled = safeIndex < effectiveCards.size - 1
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next Prompt")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Academic Knowledge Taxonomy Matrix.
 * Organizes flashcards and knowledge axioms into formal intellectual domains.
 */
@Composable
private fun KnowledgeTaxonomyMatrixView(
    cards: List<FlashcardEntity>,
    notes: List<NoteEntity>,
    viewModel: MainViewModel
) {
    if (cards.isEmpty()) {
        EmptyRecallState()
        return
    }

    var selectedDomain by remember { mutableStateOf("ALL") }

    // Group cards by conceptKey
    val concepts = remember(cards) {
        cards.map { it.conceptKey.ifBlank { "General" } }.distinct().sorted()
    }

    val filteredCards = remember(cards, selectedDomain) {
        if (selectedDomain == "ALL") cards else cards.filter { it.conceptKey.equals(selectedDomain, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "KNOWLEDGE DOMAINS",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedDomain == "ALL",
                        onClick = { selectedDomain = "ALL" },
                        label = { Text("All Domains (${cards.size})", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
                items(concepts) { concept ->
                    val domainCount = cards.count { it.conceptKey.equals(concept, ignoreCase = true) }
                    val domainMastered = cards.count { it.conceptKey.equals(concept, ignoreCase = true) && it.isMastered }
                    FilterChip(
                        selected = selectedDomain.equals(concept, ignoreCase = true),
                        onClick = { selectedDomain = concept },
                        label = { Text("$concept ($domainMastered/$domainCount)", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }

        items(filteredCards, key = { it.id }) { card ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (card.isMastered) Color(0xFF10B981).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = card.conceptKey.ifBlank { "CONCEPT" }.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp
                        )
                        if (card.isMastered) {
                            Text(
                                text = "✓ MASTERED",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cleanAsterisks(card.question),
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (expanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = cleanAsterisks(card.answer),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 19.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.markFlashcardReviewed(card.id, !card.isMastered)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (card.isMastered) "Mark Unmastered" else "Mark Mastered", fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Ebbinghaus Continuum Deck & Memory Consolidation Telemetry.
 */
@Composable
private fun MemoryContinuumDeckView(
    cards: List<FlashcardEntity>,
    notes: List<NoteEntity>,
    retentionHealth: Int,
    shortTermCount: Int,
    longTermCount: Int,
    masteredCards: Int,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ebbinghaus Consolidation Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EBBINGHAUS MEMORY DECAY METRICS",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 9.5.sp
                        )
                        Text(
                            text = "$retentionHealth% STABILITY",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RetentionGold,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(primaryColor.copy(alpha = 0.1f))
                                .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("BUFFER", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = primaryColor)
                                Text("$shortTermCount Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RetentionGold.copy(alpha = 0.1f))
                                .border(1.dp, RetentionGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("LONG-TERM", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = RetentionGold)
                                Text("$longTermCount Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("MASTERED", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF10B981))
                                Text("$masteredCards / ${cards.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // One-Tap Cognitive Consolidation Blast
                    Button(
                        onClick = {
                            viewModel.consolidateAllNotes()
                            Toast.makeText(context, "Memory consolidated to long-term storage!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Consolidate All to Long-Term Memory", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = "CONTINUUM REVISION CARDS (${cards.size})",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }

        if (cards.isEmpty()) {
            item { EmptyRecallState() }
        } else {
            items(cards, key = { it.id }) { card ->
                var revealed by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { revealed = !revealed },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = card.conceptKey.ifBlank { "CONCEPT" }.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = secondaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp
                            )
                            if (card.isMastered) {
                                Text(
                                    text = "✓ Mastered",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cleanAsterisks(card.question),
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        if (revealed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = cleanAsterisks(card.answer),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRecallState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "NO ACTIVE RECALL PROMPTS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Record or ingest cognitive notes to automatically synthesize Socratic drill cards.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Tab 1: Interactive Quizzes & Assessments Lab
 */
@Composable
private fun QuizzesAssessmentsLabView(
    viewModel: MainViewModel,
    notes: List<NoteEntity>
) {
    val activeQuiz by viewModel.activeQuiz.collectAsState()
    val questions by viewModel.activeQuizQuestions.collectAsState()
    val allQuizzes by viewModel.allQuizzes.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary

    if (activeQuiz != null) {
        val currentQuiz = activeQuiz!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.clearActiveQuiz() },
                        modifier = Modifier.size(36.dp).testTag("action_back_quiz_list")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to quizzes")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = currentQuiz.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentQuiz.topic.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = primaryColor,
                            fontSize = 9.sp
                        )
                    }
                }

                if (currentQuiz.isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Score: ${currentQuiz.scoreAchieved}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Score Banner if completed
            if (currentQuiz.isCompleted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val score = currentQuiz.scoreAchieved ?: 0
                            val tier = when {
                                score >= 80 -> "Mastery Achieved ★★★"
                                score >= 60 -> "Proficient ★★☆"
                                else -> "Review Recommended ★☆☆"
                            }
                            Text(
                                text = tier,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (score >= 60) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Text(
                                text = "Assessment evaluated. Review answers below or retake.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.selectQuiz(currentQuiz)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("action_retake_quiz")
                        ) {
                            Text("Retake", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Questions list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(questions.size) { idx ->
                    val q = questions[idx]
                    val options = remember(q.optionsJson) {
                        try {
                            val arr = JSONArray(q.optionsJson)
                            List(arr.length()) { i -> arr.getString(i) }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "QUESTION ${idx + 1} OF ${questions.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )

                                if (q.userSelectedOptionIndex != -1) {
                                    val isCorrect = q.userSelectedOptionIndex == q.correctOptionIndex
                                    Text(
                                        text = if (isCorrect) "✓ CORRECT" else "✗ INCORRECT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = cleanAsterisks(q.questionText),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            options.forEachIndexed { optIdx, optText ->
                                val isSelected = q.userSelectedOptionIndex == optIdx
                                val isCorrectOption = optIdx == q.correctOptionIndex
                                val isAnswered = q.userSelectedOptionIndex != -1

                                val optionBorderColor = when {
                                    !isAnswered && isSelected -> primaryColor
                                    isAnswered && isCorrectOption -> Color(0xFF10B981)
                                    isAnswered && isSelected && !isCorrectOption -> Color(0xFFEF4444)
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                }

                                val optionBgColor = when {
                                    isAnswered && isCorrectOption -> Color(0xFF10B981).copy(alpha = 0.12f)
                                    isAnswered && isSelected && !isCorrectOption -> Color(0xFFEF4444).copy(alpha = 0.12f)
                                    isSelected -> primaryColor.copy(alpha = 0.08f)
                                    else -> MaterialTheme.colorScheme.surface
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(optionBgColor)
                                        .border(1.dp, optionBorderColor, RoundedCornerShape(8.dp))
                                        .clickable(enabled = !currentQuiz.isCompleted) {
                                            viewModel.answerQuizQuestion(q.id, optIdx)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                if (!currentQuiz.isCompleted) {
                                                    viewModel.answerQuizQuestion(q.id, optIdx)
                                                }
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cleanAsterisks(optText),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Show explanation if question is answered
                            if (q.userSelectedOptionIndex != -1 && q.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "EXPLANATION",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor,
                                            fontSize = 8.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = cleanAsterisks(q.explanation),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!currentQuiz.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.finishQuiz(currentQuiz.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_submit_quiz"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Complete & Calculate Score", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // Quizzes list & note assessment generator
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick generator header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI QUIZZES & ASSESSMENTS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Synthesize multi-question diagnostic quizzes directly from your ingested knowledge to verify conceptual mastery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        if (notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "QUICK GENERATE FROM RECENT NOTE:",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 8.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(notes.take(5)) { note ->
                                    OutlinedButton(
                                        onClick = { viewModel.generateQuizForNote(note) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = note.title.take(18) + if (note.title.length > 18) "..." else "",
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quizzes Library
            item {
                Text(
                    text = "ASSESSMENT ARCHIVE (${allQuizzes.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.5.sp
                )
            }

            if (allQuizzes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No assessments created yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap any note above to automatically synthesize a diagnostic quiz.",
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            } else {
                items(allQuizzes) { quiz ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectQuiz(quiz) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = quiz.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = quiz.topic.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = primaryColor,
                                        fontSize = 8.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${quiz.totalQuestions} questions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            if (quiz.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${quiz.scoreAchieved}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.selectQuiz(quiz) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Start", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Personalized Study Plans & Exam Roadmaps
 */
@Composable
private fun StudyPlansLabView(
    viewModel: MainViewModel
) {
    val allPlans by viewModel.allStudyPlans.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    var showCreateModal by remember { mutableStateOf(false) }

    var subjectInput by remember { mutableStateOf("") }
    var examDateInput by remember { mutableStateOf("2026-12-15") }
    var hoursInput by remember { mutableIntStateOf(2) }

    if (showCreateModal) {
        AlertDialog(
            onDismissRequest = { showCreateModal = false },
            title = {
                Text(
                    text = "Synthesize Study Roadmap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Specify your target subject, deadline date, and study availability to generate an AI curriculum.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = subjectInput,
                        onValueChange = { subjectInput = it },
                        label = { Text("Subject / Exam Topic") },
                        placeholder = { Text("e.g. Distributed Systems Architecture") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = examDateInput,
                        onValueChange = { examDateInput = it },
                        label = { Text("Target Exam / Deadline Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Study: $hoursInput hours/day",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(onClick = { if (hoursInput > 1) hoursInput-- }) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { if (hoursInput < 10) hoursInput++ }) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectInput.isNotBlank()) {
                            viewModel.createStudyPlan(subjectInput.trim(), examDateInput.trim(), hoursInput)
                            showCreateModal = false
                            subjectInput = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Generate Roadmap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Generator Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PERSONALIZED STUDY PLANS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "AI-tailored curriculum roadmaps and milestone trackers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showCreateModal = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("action_create_study_plan")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Plan", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = "ACTIVE STUDY ROADMAPS (${allPlans.size})",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.5.sp
            )
        }

        if (allPlans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No study plans created yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'New Plan' to synthesize a tailored study schedule for upcoming exams.",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        } else {
            items(allPlans) { plan ->
                val phases = remember(plan.scheduleJson) {
                    try {
                        val arr = JSONArray(plan.scheduleJson)
                        List(arr.length()) { i ->
                            val obj = arr.getJSONObject(i)
                            val phase = obj.optString("phase", "Phase ${i + 1}")
                            val duration = obj.optString("duration", "")
                            val topicsArr = obj.optJSONArray("topics")
                            val topics = if (topicsArr != null) List(topicsArr.length()) { idx -> topicsArr.getString(idx) } else emptyList()
                            val actsArr = obj.optJSONArray("activities")
                            val acts = if (actsArr != null) List(actsArr.length()) { idx -> actsArr.getString(idx) } else emptyList()
                            Triple(phase, duration, Pair(topics, acts))
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plan.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${plan.subject.uppercase()} • EXAM: ${plan.examDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = primaryColor,
                                        fontSize = 8.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${plan.hoursPerDay}h/day",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.toggleStudyPlan(plan) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (plan.isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                                        contentDescription = "Toggle completion",
                                        tint = if (plan.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteStudyPlan(plan) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete plan",
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }

                        if (plan.goals.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "CORE OBJECTIVE: ${cleanAsterisks(plan.goals)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        // Structured Timeline Phases
                        if (phases.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "CURRICULUM PHASES & MILESTONES",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 8.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            phases.forEach { (phaseName, duration, details) ->
                                val (topics, activities) = details
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = phaseName,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = duration,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = primaryColor,
                                                fontSize = 9.sp
                                            )
                                        }

                                        if (topics.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Topics: ${topics.joinToString(", ")}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.5.sp
                                            )
                                        }

                                        if (activities.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Activities: ${activities.joinToString("; ")}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
