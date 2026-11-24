package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AskNeuroScribeMode
import com.example.data.model.ChatMessageEntity
import com.example.ui.components.SpreadedAnimatedBackground
import com.example.ui.components.cleanAsterisks
import com.example.ui.theme.MindViolet
import com.example.ui.theme.NeuroCyan
import com.example.ui.theme.RetentionGold
import com.example.ui.viewmodel.AiProcessingState
import com.example.ui.viewmodel.MainViewModel
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SuggestionPrompt(
    val title: String,
    val subtitle: String,
    val prompt: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun AskNeuroScribeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val askState by viewModel.askAiState.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()

    var queryInput by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(AskNeuroScribeMode.MY_KNOWLEDGE) }
    val listState = rememberLazyListState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    LaunchedEffect(chatMessages.size, askState) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val suggestionCards = remember {
        listOf(
            SuggestionPrompt(
                title = "Summarize Vault Knowledge",
                subtitle = "Synthesize across all recorded audio & notes",
                prompt = "Summarize the key knowledge and core concepts across all my saved notes",
                icon = Icons.Default.Psychology,
                color = MindViolet
            ),
            SuggestionPrompt(
                title = "Thematic Trend Analysis",
                subtitle = "Detect patterns and recurring insights",
                prompt = "Analyze the overarching themes and analytical patterns in my recordings",
                icon = Icons.Default.Analytics,
                color = NeuroCyan
            ),
            SuggestionPrompt(
                title = "Exam & Recall Mastery Drills",
                subtitle = "Generate Socratic questions from my vault",
                prompt = "Create 4 deep Socratic active recall questions to test my retention on my notes",
                icon = Icons.Default.School,
                color = Color(0xFF10B981)
            ),
            SuggestionPrompt(
                title = "Actionable Roadmap & Next Steps",
                subtitle = "Translate spoken insights into execution",
                prompt = "Extract an actionable, prioritized execution roadmap from my recent notes",
                icon = Icons.Default.Lightbulb,
                color = RetentionGold
            )
        )
    }

    androidx.activity.compose.BackHandler {
        viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.HOME)
    }

    SpreadedAnimatedBackground(
        modifier = modifier.fillMaxSize(),
        isDarkTheme = isDarkTheme
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.HOME) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Back to Home",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(primaryColor, MindViolet)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ASK NEUROSCRIBE",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RetentionGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "PRO AI",
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = RetentionGold
                                )
                            }
                        }
                        Text(
                            text = "Executive Intelligence Q&A",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (chatMessages.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Dual Mode Selector Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMode == AskNeuroScribeMode.MY_KNOWLEDGE,
                    onClick = { selectedMode = AskNeuroScribeMode.MY_KNOWLEDGE },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = { Text("Mode A: Vault Grounding", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = selectedMode == AskNeuroScribeMode.ADVANCED_INTELLIGENCE,
                    onClick = { selectedMode = AskNeuroScribeMode.ADVANCED_INTELLIGENCE },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = { Text("Mode B: Global Synthesis", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MindViolet,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // Mode Context Explainer
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selectedMode == AskNeuroScribeMode.MY_KNOWLEDGE) Icons.Default.Check else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (selectedMode == AskNeuroScribeMode.MY_KNOWLEDGE) primaryColor else MindViolet,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedMode == AskNeuroScribeMode.MY_KNOWLEDGE)
                            "Grounded exclusively in your ${allNotes.size} saved documents with exact citations."
                        else
                            "Combining your personal vault with global state-of-the-art scientific and cognitive intelligence.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Main Message Feed or Empty State
            if (chatMessages.isEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.12f))
                                    .border(1.5.dp, primaryColor.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Query Your Cognitive Vault",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ask questions, generate masteries, or synthesize insights across ${allNotes.size} recordings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    item {
                        Text(
                            text = "SUGGESTED EXECUTIVE INQUIRIES",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    items(suggestionCards) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    queryInput = item.prompt
                                    viewModel.askNeuroScribe(item.prompt, selectedMode)
                                    queryInput = ""
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(item.color.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = item.color,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(chatMessages, key = { it.id }) { message ->
                        ChatMessageBubble(
                            message = message,
                            onOpenDoc = { docId ->
                                val target = allNotes.find { it.id == docId }
                                if (target != null) {
                                    viewModel.selectNote(target)
                                }
                            }
                        )
                    }

                    if (askState is AiProcessingState.Processing) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp),
                                        color = primaryColor
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "NeuroScribe Pro Reasoning Engine",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = primaryColor
                                        )
                                        Text(
                                            text = (askState as AiProcessingState.Processing).stageMessage,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Always Visible, Elevated Typing Bar Docked Above Soft Keyboard
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = queryInput,
                        onValueChange = { queryInput = it },
                        placeholder = {
                            Text(
                                text = if (selectedMode == AskNeuroScribeMode.MY_KNOWLEDGE)
                                    "Ask personal vault notes & transcripts..."
                                else
                                    "Ask advanced cognitive synthesis...",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (selectedMode == AskNeuroScribeMode.MY_KNOWLEDGE) Icons.Default.Description else Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (selectedMode == AskNeuroScribeMode.MY_KNOWLEDGE) primaryColor else MindViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (queryInput.isNotBlank()) {
                                IconButton(onClick = { queryInput = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear input",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ask_neuroscribe_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = primaryColor
                        ),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                val q = queryInput.trim()
                                if (q.isNotBlank()) {
                                    queryInput = ""
                                    focusManager.clearFocus()
                                    viewModel.askNeuroScribe(q, selectedMode)
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSend = queryInput.isNotBlank() && askState !is AiProcessingState.Processing
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (canSend) {
                                    Brush.linearGradient(listOf(primaryColor, MindViolet))
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            primaryColor.copy(alpha = 0.35f),
                                            MindViolet.copy(alpha = 0.35f)
                                        )
                                    )
                                }
                            )
                            .clickable(enabled = canSend) {
                                val q = queryInput.trim()
                                if (q.isNotBlank()) {
                                    queryInput = ""
                                    focusManager.clearFocus()
                                    viewModel.askNeuroScribe(q, selectedMode)
                                }
                            }
                            .testTag("ask_neuroscribe_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Prompt",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    onOpenDoc: (Long) -> Unit
) {
    val context = LocalContext.current
    val isUser = message.role == "user"
    val primaryColor = MaterialTheme.colorScheme.primary

    val citationsList = remember(message.citationsJson) {
        val list = mutableListOf<Triple<Long, String, String>>()
        try {
            val arr = JSONArray(message.citationsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Triple(
                        obj.optLong("noteId", 0L),
                        obj.optString("title", "Document"),
                        obj.optString("excerpt", "")
                    )
                )
            }
        } catch (e: Exception) {
            // ignore
        }
        list
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // User Query Bubble
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp),
                color = primaryColor,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = cleanAsterisks(message.content),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        } else {
            // AI Cognitive Masterpiece Response Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header metadata strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (message.mode == "MY_KNOWLEDGE") primaryColor.copy(alpha = 0.15f) else MindViolet.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (message.mode == "MY_KNOWLEDGE") Icons.Default.Description else Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = if (message.mode == "MY_KNOWLEDGE") primaryColor else MindViolet,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (message.mode == "MY_KNOWLEDGE") "VAULT GROUNDED ANSWER" else "ADVANCED COGNITIVE SYNTHESIS",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (message.mode == "MY_KNOWLEDGE") primaryColor else MindViolet,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Structured Content Display
                    StructuredAnswerRenderer(content = cleanAsterisks(message.content))

                    // Document Citations
                    if (citationsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "GROUNDED SOURCE CITATIONS",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        citationsList.forEach { (docId, title, excerpt) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = primaryColor.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { onOpenDoc(docId) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = primaryColor
                                        )
                                        if (excerpt.isNotBlank()) {
                                            Text(
                                                text = excerpt,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Bar: Copy and Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("NeuroScribe AI", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Synthesis copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, message.content)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share AI Synthesis"))
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StructuredAnswerRenderer(content: String) {
    val lines = content.lines()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("###") -> {
                    val heading = line.removePrefix("###").trim()
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = heading,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                color = primaryColor,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
                line.startsWith("##") -> {
                    val heading = line.removePrefix("##").trim()
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = heading,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.2.sp
                    )
                }
                line.startsWith("> ") -> {
                    // Executive Highlight Blockquote
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = secondaryColor.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, secondaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(secondaryColor, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = line.removePrefix("> ").trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
                line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") -> {
                    val rawBullet = line.substring(2).trim()
                    val colonIdx = rawBullet.indexOf(':')

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp, top = 2.dp)
                    ) {
                        Text(
                            text = "•",
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        if (colonIdx != -1 && colonIdx < 45) {
                            // High-yield structured bullet with Bolded Leading Key Point
                            val leadingTitle = rawBullet.substring(0, colonIdx).trim()
                            val bodyText = rawBullet.substring(colonIdx + 1).trim()

                            val styledText = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                                    append(leadingTitle)
                                    append(": ")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color.Unspecified)) {
                                    append(bodyText)
                                }
                            }

                            Text(
                                text = styledText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 21.sp
                            )
                        } else {
                            Text(
                                text = rawBullet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 21.sp
                            )
                        }
                    }
                }
                line.matches(Regex("^\\d+\\..*")) -> {
                    // Numbered Steps with Pill
                    val dotIdx = line.indexOf('.')
                    val stepNum = line.substring(0, dotIdx).trim()
                    val restText = line.substring(dotIdx + 1).trim()
                    val colonIdx = restText.indexOf(':')

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stepNum,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        if (colonIdx != -1 && colonIdx < 45) {
                            val title = restText.substring(0, colonIdx).trim()
                            val desc = restText.substring(colonIdx + 1).trim()

                            val styledStep = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                                    append(title)
                                    append(": ")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color.Unspecified)) {
                                    append(desc)
                                }
                            }

                            Text(
                                text = styledStep,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 21.sp
                            )
                        } else {
                            Text(
                                text = restText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 21.sp
                            )
                        }
                    }
                }
                line.isNotBlank() -> {
                    val colonIdx = line.indexOf(':')
                    if (colonIdx in 1..40 && !line.startsWith("http")) {
                        val title = line.substring(0, colonIdx).trim()
                        val rest = line.substring(colonIdx + 1).trim()
                        val styledLine = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                                append(title)
                                append(": ")
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color.Unspecified)) {
                                append(rest)
                            }
                        }
                        Text(
                            text = styledLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    } else {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}
