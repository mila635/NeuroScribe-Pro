package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.example.ui.viewmodel.AppScreen
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KnowledgeConnectionEntity
import com.example.data.model.NoteEntity
import com.example.ui.components.ConceptMindMapView
import com.example.ui.components.ConceptNode
import com.example.ui.components.SpreadedAnimatedBackground
import com.example.ui.components.cleanAsterisks
import com.example.ui.theme.MindViolet
import com.example.ui.theme.RetentionGold
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KnowledgeVaultScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val connections by viewModel.allKnowledgeConnections.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Concept Network, 1: Subject Clusters, 2: Vault Index
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Compute distinct categories and tag distribution
    val categories = listOf("All") + allNotes.map { it.category }.distinct().filter { it.isNotBlank() }
    val allTags = allNotes.flatMap { note ->
        note.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }.distinct()

    SpreadedAnimatedBackground(
        modifier = modifier.fillMaxSize(),
        isDarkTheme = isDarkTheme
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PERSONAL KNOWLEDGE VAULT",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Knowledge Architecture",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { viewModel.discoverAndSaveKnowledgeConnections() },
                    modifier = Modifier.testTag("discover_connections_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Discover Connections",
                        tint = primaryColor
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = primaryColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = primaryColor
                    )
                },
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Concept Graph", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Subject Clusters", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Vault Index", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (activeTab) {
                0 -> ConceptGraphTab(
                    viewModel = viewModel,
                    notes = allNotes,
                    connections = connections,
                    onDiscover = { viewModel.discoverAndSaveKnowledgeConnections() }
                )
                1 -> SubjectClustersTab(
                    viewModel = viewModel,
                    notes = allNotes,
                    categories = categories,
                    selectedCategory = selectedCategoryFilter,
                    onSelectCategory = { selectedCategoryFilter = it }
                )
                2 -> VaultIndexTab(
                    notes = allNotes,
                    tags = allTags,
                    connectionsCount = connections.size
                )
            }
        }
    }
}

@Composable
private fun ConceptGraphTab(
    viewModel: MainViewModel,
    notes: List<NoteEntity>,
    connections: List<KnowledgeConnectionEntity>,
    onDiscover: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    var viewMode by remember { mutableIntStateOf(0) } // 0: Interactive 2D Graph, 1: Semantic Link Index
    var selectedNodeTitle by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Mode Toggle Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cognitive Knowledge Map",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${notes.size} docs • ${connections.size} semantic concept bridges",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onDiscover,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Map Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // View Mode Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (viewMode == 0) primaryColor else Color.Transparent)
                                .clickable { viewMode = 0 }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "2D Concept Graph",
                                fontSize = 11.sp,
                                fontWeight = if (viewMode == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (viewMode == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (viewMode == 1) primaryColor else Color.Transparent)
                                .clickable { viewMode = 1 }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Semantic Link List",
                                fontSize = 11.sp,
                                fontWeight = if (viewMode == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (viewMode == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (viewMode == 0) {
            // Interactive 2D Conceptual Graph Canvas
            item {
                InteractiveConceptGraphCanvas(
                    notes = notes,
                    connections = connections,
                    selectedTitle = selectedNodeTitle,
                    onSelectNode = { title -> selectedNodeTitle = title }
                )
            }

            // Selected Node Inspector Card
            if (selectedNodeTitle != null) {
                val selectedNote = notes.find { it.title.equals(selectedNodeTitle, ignoreCase = true) }
                val connectedEdges = connections.filter { conn ->
                    val s = notes.find { it.id == conn.sourceNoteId }?.title
                    val t = notes.find { it.id == conn.targetNoteId }?.title
                    conn.sharedConcept.equals(selectedNodeTitle, true) || s.equals(selectedNodeTitle, true) || t.equals(selectedNodeTitle, true)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, primaryColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Hub,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedNodeTitle.orEmpty(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(
                                    onClick = { selectedNodeTitle = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Close inspector",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (selectedNote != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = cleanAsterisks(selectedNote.summary.ifBlank { selectedNote.synthesizedContent.take(150) }),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.selectNote(selectedNote) },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Open Full Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.navigateTo(AppScreen.ASK_AI)
                                            viewModel.askNeuroScribe("Explain concept: ${selectedNote.title}", com.example.ai.AskNeuroScribeMode.MY_KNOWLEDGE)
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Ask AI About Node", fontSize = 11.sp)
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Concept Bridge connected across ${connectedEdges.size} recorded documents in your vault.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Semantic Link List Mode
            if (connections.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Document Connections Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap 'Map Vault' to analyze your documents and bridge related concepts across recordings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            OutlinedButton(onClick = onDiscover) {
                                Text("Map Vault Connections")
                            }
                        }
                    }
                }
            } else {
                items(connections) { connection ->
                    val sourceNote = notes.find { it.id == connection.sourceNoteId }
                    val targetNote = notes.find { it.id == connection.targetNoteId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = primaryColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = connection.relationshipType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = primaryColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Bridge: ${connection.sharedConcept}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { sourceNote?.let { viewModel.selectNote(it) } }
                                ) {
                                    Text(
                                        text = sourceNote?.title ?: "Document #${connection.sourceNoteId}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = sourceNote?.category ?: "Source",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Connects to",
                                    tint = primaryColor,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .size(16.dp)
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { targetNote?.let { viewModel.selectNote(it) } }
                                ) {
                                    Text(
                                        text = targetNote?.title ?: "Document #${connection.targetNoteId}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = targetNote?.category ?: "Target",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * 2D Interactive Concept Network Graph Canvas
 * Renders nodes, dynamic connection lines, and handles tap hit-testing to inspect nodes.
 */
@Composable
private fun InteractiveConceptGraphCanvas(
    notes: List<NoteEntity>,
    connections: List<KnowledgeConnectionEntity>,
    selectedTitle: String?,
    onSelectNode: (String) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    // Build nodes: combine notes and prominent shared concepts
    data class GraphNode(
        val title: String,
        val isNote: Boolean,
        val category: String,
        val color: Color
    )

    val graphNodes = remember(notes, connections) {
        val list = mutableListOf<GraphNode>()
        notes.take(8).forEach { note ->
            list.add(
                GraphNode(
                    title = note.title,
                    isNote = true,
                    category = note.category,
                    color = primaryColor
                )
            )
        }
        connections.map { it.sharedConcept }.distinct().take(4).forEach { concept ->
            if (list.none { it.title.equals(concept, true) }) {
                list.add(
                    GraphNode(
                        title = concept,
                        isNote = false,
                        category = "Concept",
                        color = secondaryColor
                    )
                )
            }
        }
        list
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (graphNodes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No notes to map yet. Record or import notes to generate 2D concept network.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(graphNodes) {
                            detectTapGestures { tapOffset ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val centerX = w / 2f
                                val centerY = h / 2f
                                val radius = (minOf(w, h) * 0.38f)

                                graphNodes.forEachIndexed { i, node ->
                                    val angle = (2 * Math.PI * i / graphNodes.size).toFloat()
                                    val nx = centerX + radius * kotlin.math.cos(angle.toDouble()).toFloat()
                                    val ny = centerY + radius * kotlin.math.sin(angle.toDouble()).toFloat()
                                    val dist = kotlin.math.sqrt((tapOffset.x - nx) * (tapOffset.x - nx) + (tapOffset.y - ny) * (tapOffset.y - ny))
                                    if (dist <= 30f) {
                                        onSelectNode(node.title)
                                    }
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val centerX = w / 2f
                    val centerY = h / 2f
                    val orbitRadius = (minOf(w, h) * 0.38f)

                    // Draw subtle grid orbit circles
                    drawCircle(
                        color = outlineColor.copy(alpha = 0.3f),
                        radius = orbitRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.2f)
                    )
                    drawCircle(
                        color = outlineColor.copy(alpha = 0.15f),
                        radius = orbitRadius * 0.55f,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1f)
                    )

                    // Draw Central Hub Node
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.2f),
                        radius = 24f,
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 12f,
                        center = Offset(centerX, centerY)
                    )

                    // Compute node positions
                    val positions = graphNodes.indices.map { i ->
                        val angle = (2 * Math.PI * i / graphNodes.size).toFloat()
                        Offset(
                            x = centerX + orbitRadius * kotlin.math.cos(angle.toDouble()).toFloat(),
                            y = centerY + orbitRadius * kotlin.math.sin(angle.toDouble()).toFloat()
                        )
                    }

                    // Draw connecting lines between adjacent nodes and to center
                    positions.forEachIndexed { i, pos ->
                        // Line to central hub
                        drawLine(
                            color = primaryColor.copy(alpha = 0.25f),
                            start = Offset(centerX, centerY),
                            end = pos,
                            strokeWidth = 1.5f,
                            cap = StrokeCap.Round
                        )

                        // Connections between nodes
                        if (positions.size > 1) {
                            val nextPos = positions[(i + 1) % positions.size]
                            drawLine(
                                color = secondaryColor.copy(alpha = 0.2f),
                                start = pos,
                                end = nextPos,
                                strokeWidth = 1.2f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Draw individual nodes
                    positions.forEachIndexed { i, pos ->
                        val node = graphNodes[i]
                        val isSelected = node.title.equals(selectedTitle, ignoreCase = true)

                        if (isSelected) {
                            // Glowing halo
                            drawCircle(
                                color = node.color.copy(alpha = 0.35f),
                                radius = 22f,
                                center = pos
                            )
                        }

                        drawCircle(
                            color = if (node.isNote) primaryColor else secondaryColor,
                            radius = if (isSelected) 14f else 9f,
                            center = pos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 5f else 3f,
                            center = pos
                        )
                    }
                }

                // Legend overlay in bottom corner
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(primaryColor))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Doc Node", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(secondaryColor))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Concept Bridge", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectClustersTab(
    viewModel: MainViewModel,
    notes: List<NoteEntity>,
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val filteredNotes = if (selectedCategory == "All") notes else notes.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onSelectCategory(category) },
                    label = { Text(category, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredNotes) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectNote(note) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = note.category,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = if (note.isLongTermMemory) "Long-Term Vault" else "Sensory Buffer",
                                fontSize = 11.sp,
                                color = if (note.isLongTermMemory) RetentionGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = note.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = cleanAsterisks(note.summary.ifBlank { note.synthesizedContent.take(120) }),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VaultIndexTab(
    notes: List<NoteEntity>,
    tags: List<String>,
    connectionsCount: Int
) {
    val totalWords = notes.sumOf { it.synthesizedContent.split(Regex("\\s+")).size }
    val longTermCount = notes.count { it.isLongTermMemory }
    val shortTermCount = notes.size - longTermCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VAULT RETRIEVAL TELEMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Indexed Words", fontSize = 13.sp)
                        Text("$totalWords words", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Documents", fontSize = 13.sp)
                        Text("${notes.size} documents", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Long-Term Consolidated", fontSize = 13.sp)
                        Text("$longTermCount notes", fontWeight = FontWeight.Bold, color = RetentionGold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sensory Buffer", fontSize = 13.sp)
                        Text("$shortTermCount notes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cross-Document Bridges", fontSize = 13.sp)
                        Text("$connectionsCount links", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOPIC INDEX & KEYWORD TAGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (tags.isEmpty()) {
                        Text(
                            text = "No indexed tags yet. Tags are automatically extracted during cognitive synthesis.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tags.take(30).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
