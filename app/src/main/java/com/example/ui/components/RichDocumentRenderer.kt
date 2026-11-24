package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.UrduNastaliqFontFamily
import com.example.ui.theme.isUrduText
import java.net.URLEncoder

/**
 * Strips all raw markdown asterisks (*, **, ***) and cleans text.
 */
fun cleanAsterisks(text: String): String {
    return text
        .replace(Regex("\\*+"), "")
        .replace("`", "")
        .trim()
}

/**
 * Parses inline markdown bold patterns (like **text**) into AnnotatedString with bold weight
 * while completely removing the literal asterisks (* or **).
 */
fun parseMarkdownToAnnotatedString(
    text: String,
    normalColor: Color,
    boldColor: Color
): AnnotatedString {
    val cleanLine = text.replace("`", "")
    val builder = AnnotatedString.Builder()

    // Pattern matches **bold text** or *italic text*
    val regex = Regex("\\*\\*(.*?)\\*\\*|\\*(.*?)\\*")
    var lastIndex = 0

    val matches = regex.findAll(cleanLine)
    for (match in matches) {
        if (match.range.first > lastIndex) {
            val preceding = cleanLine.substring(lastIndex, match.range.first)
            builder.withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                append(preceding)
            }
        }

        val boldContent = match.groups[1]?.value ?: match.groups[2]?.value ?: ""
        builder.withStyle(SpanStyle(color = boldColor, fontWeight = FontWeight.Bold)) {
            append(boldContent)
        }

        lastIndex = match.range.last + 1
    }

    if (lastIndex < cleanLine.length) {
        val remainder = cleanLine.substring(lastIndex)
        val colonIdx = remainder.indexOf(":")
        if (colonIdx in 1..40 && !remainder.startsWith("http") && matches.count() == 0) {
            val keyPart = remainder.substring(0, colonIdx + 1)
            val valPart = remainder.substring(colonIdx + 1)
            builder.withStyle(SpanStyle(color = boldColor, fontWeight = FontWeight.Bold)) {
                append(keyPart)
            }
            builder.withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                append(valPart)
            }
        } else {
            builder.withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                append(remainder)
            }
        }
    }

    return builder.toAnnotatedString()
}

/**
 * Masterpiece Rich Document Renderer.
 * - Bold, clear typography without asterisks
 * - Fully clickable hyperlinks (YouTube tutorials, Wikipedia, official docs)
 * - ASCII flowchart and architecture diagram containers with horizontal scrolling
 * - Interactive External Study Resources Action Hub
 */
@Composable
fun RichDocumentRenderer(
    content: String,
    noteTitle: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Extract clean topic subject for the External Study Resources Hub
    val docTitle = extractCleanSubject(noteTitle, content)

    val elements = parseContentToElements(content)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (element in elements) {
            when (element) {
                is DocElement.Divider -> {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                is DocElement.Heading1 -> {
                    val isUrdu = isUrduText(element.text)
                    Text(
                        text = element.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                        fontSize = if (isUrdu) 22.sp else 20.sp,
                        lineHeight = if (isUrdu) 36.sp else 26.sp,
                        textAlign = if (isUrdu) TextAlign.End else TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                is DocElement.Heading2 -> {
                    val isUrdu = isUrduText(element.text)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isUrdu) {
                            Box(
                                modifier = Modifier
                                    .width(3.5.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(primaryColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = element.text,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = if (isUrdu) 19.sp else 16.5.sp,
                            lineHeight = if (isUrdu) 32.sp else 23.sp,
                            textAlign = if (isUrdu) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )
                        if (isUrdu) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(3.5.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(primaryColor)
                            )
                        }
                    }
                }

                is DocElement.Heading3 -> {
                    val isUrdu = isUrduText(element.text)
                    Text(
                        text = element.text,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = secondaryColor,
                        fontSize = if (isUrdu) 17.sp else 15.sp,
                        lineHeight = if (isUrdu) 28.sp else 20.sp,
                        textAlign = if (isUrdu) TextAlign.End else TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                }

                is DocElement.Blockquote -> {
                    val isUrdu = isUrduText(element.text)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .border(1.dp, primaryColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = element.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurfaceVariant,
                            fontSize = if (isUrdu) 14.sp else 12.5.sp,
                            fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Monospace,
                            lineHeight = if (isUrdu) 28.sp else 18.sp,
                            textAlign = if (isUrdu) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is DocElement.CodeBlock -> {
                    StructuredVisualizerCard(diagramText = element.text)
                }

                is DocElement.Table -> {
                    AcademicTableCard(rows = element.rows)
                }

                is DocElement.CheckboxItem -> {
                    val annotated = parseMarkdownToAnnotatedString(element.text, onSurface, primaryColor)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (element.isChecked) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (element.isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = annotated,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                is DocElement.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        if (element.text.contains(Regex("\\[(.*?)\\]\\((https?://.*?)\\)"))) {
                            ClickableMarkdownLine(
                                lineText = element.text,
                                onSurface = onSurface,
                                linkColor = primaryColor,
                                onOpenUrl = { url -> openSafeUri(context, uriHandler, url) }
                            )
                        } else {
                            val annotated = parseMarkdownToAnnotatedString(element.text, onSurface, primaryColor)
                            val isUrdu = isUrduText(element.text)
                            Text(
                                text = annotated,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Default,
                                lineHeight = if (isUrdu) 28.sp else 21.sp,
                                fontSize = if (isUrdu) 15.sp else 14.sp,
                                textAlign = if (isUrdu) TextAlign.End else TextAlign.Start,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                is DocElement.NumberedItem -> {
                    val annotated = parseMarkdownToAnnotatedString(element.text, onSurface, primaryColor)
                    val isUrdu = isUrduText(element.text)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = element.number,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 14.sp,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = annotated,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Default,
                            lineHeight = if (isUrdu) 28.sp else 21.sp,
                            fontSize = if (isUrdu) 15.sp else 14.sp,
                            textAlign = if (isUrdu) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is DocElement.LinkItem -> {
                    ClickableMarkdownLine(
                        lineText = element.text,
                        onSurface = onSurface,
                        linkColor = primaryColor,
                        onOpenUrl = { url -> openSafeUri(context, uriHandler, url) }
                    )
                }

                is DocElement.RawUrl -> {
                    ClickableUrlTile(
                        url = element.url,
                        onClick = { openSafeUri(context, uriHandler, element.url) }
                    )
                }

                is DocElement.Paragraph -> {
                    val annotated = parseMarkdownToAnnotatedString(element.text, onSurface, primaryColor)
                    val isUrdu = isUrduText(element.text)
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Default,
                        color = onSurface,
                        lineHeight = if (isUrdu) 34.sp else 23.sp,
                        fontSize = if (isUrdu) 16.sp else 14.5.sp,
                        textAlign = if (isUrdu) TextAlign.End else TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }

        // Dedicated Interactive Resources & Tutorials Action Hub
        Spacer(modifier = Modifier.height(14.dp))
        ExternalStudyResourcesCard(
            topic = docTitle,
            onOpenUrl = { url -> openSafeUri(context, uriHandler, url) }
        )
    }
}

data class FlowNode(
    val stepIndex: Int,
    val title: String,
    val subtitle: String? = null
)

data class MetricItem(
    val label: String,
    val percentage: Float,
    val displayValue: String
)

private fun isFlowchartText(text: String): Boolean {
    val lower = text.lowercase()
    val hasArrows = text.contains("->") || text.contains("-->") || text.contains("=>") || text.contains("==>") || text.contains("↓")
    val hasSteps = lower.contains("step 1") || lower.contains("phase 1") || lower.contains("stage 1") || lower.contains("process:")
    val hasAsciiBoxes = text.contains("+---") || text.contains("+===")
    return hasArrows || hasSteps || hasAsciiBoxes
}

private fun parseFlowchartNodes(text: String): List<FlowNode> {
    val nodes = mutableListOf<FlowNode>()

    // Case 1: single-line or multi-line arrow chain like [A] -> [B] -> [C]
    if (text.contains("->") || text.contains("-->") || text.contains("=>")) {
        val rawNodes = text
            .replace("-->", "->")
            .replace("==>", "->")
            .replace("=>", "->")
            .split("->")
            .map { it.replace("[", "").replace("]", "").replace("+", "").replace("|", "").trim() }
            .filter { it.isNotBlank() && !it.all { ch -> ch == '-' || ch == '=' || ch == '>' || ch == '|' } }

        if (rawNodes.size >= 2) {
            rawNodes.forEachIndexed { index, nodeStr ->
                val lines = nodeStr.lines().map { it.trim() }.filter { it.isNotBlank() }
                val title = lines.firstOrNull() ?: "Step ${index + 1}"
                val subtitle = if (lines.size > 1) lines.drop(1).joinToString(" ") else null
                nodes.add(FlowNode(stepIndex = index + 1, title = title, subtitle = subtitle))
            }
            return nodes
        }
    }

    // Case 2: step-by-step lines (e.g. Step 1: ..., Phase 1: ..., or numbered bullet stages)
    val lines = text.lines()
    var currentIndex = 1
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isBlank() || trimmed.startsWith("+") || trimmed.all { it == '-' || it == '=' || it == '|' || it == 'v' || it == 'V' || it == '.' }) continue

        val clean = trimmed.removePrefix("|").removeSuffix("|").trim()
        if (clean.isBlank()) continue

        if (clean == "v" || clean == "V" || clean == "↓" || clean == "|" || clean == "->") continue

        val colonIndex = clean.indexOf(":")
        if (colonIndex in 1..25 && (clean.startsWith("Step", ignoreCase = true) || clean.startsWith("Phase", ignoreCase = true) || clean.startsWith("Stage", ignoreCase = true))) {
            val title = clean.substring(0, colonIndex).trim()
            val sub = clean.substring(colonIndex + 1).trim()
            nodes.add(FlowNode(stepIndex = currentIndex++, title = if (sub.isNotBlank()) sub else title, subtitle = if (sub.isNotBlank()) title else null))
        } else {
            nodes.add(FlowNode(stepIndex = currentIndex++, title = clean))
        }
    }

    return if (nodes.size >= 2) nodes else emptyList()
}

private fun isMetricChartText(text: String): Boolean {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    val metricLineCount = lines.count { line ->
        line.contains("%") || line.matches(Regex(".*:\\s*\\d+(\\.\\d+)?/\\d+.*")) || line.matches(Regex(".*:\\s*\\d+(\\.\\d+)?%?"))
    }
    return metricLineCount >= 2 && metricLineCount >= (lines.size / 2)
}

private fun parseMetricItems(text: String): List<MetricItem> {
    val items = mutableListOf<MetricItem>()
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    val percentRegex = Regex("^(.*?):?\\s*(\\d+(?:\\.\\d+)?)\\s*%(.*)$")
    val ratioRegex = Regex("^(.*?):?\\s*(\\d+(?:\\.\\d+)?)\\s*/\\s*(\\d+(?:\\.\\d+)?)(.*)$")

    for (line in lines) {
        val percentMatch = percentRegex.find(line)
        if (percentMatch != null) {
            val label = percentMatch.groups[1]?.value?.trim()?.removePrefix("-")?.trim() ?: "Metric"
            val num = percentMatch.groups[2]?.value?.toFloatOrNull() ?: 0f
            val pct = (num / 100f).coerceIn(0f, 1f)
            items.add(MetricItem(label = label, percentage = pct, displayValue = "${num.toInt()}%"))
            continue
        }

        val ratioMatch = ratioRegex.find(line)
        if (ratioMatch != null) {
            val label = ratioMatch.groups[1]?.value?.trim()?.removePrefix("-")?.trim() ?: "Metric"
            val nom = ratioMatch.groups[2]?.value?.toFloatOrNull() ?: 0f
            val denom = ratioMatch.groups[3]?.value?.toFloatOrNull() ?: 10f
            val pct = if (denom > 0) (nom / denom).coerceIn(0f, 1f) else 0f
            items.add(MetricItem(label = label, percentage = pct, displayValue = "$nom / $denom"))
            continue
        }
    }
    return items
}

@Composable
private fun StructuredVisualizerCard(diagramText: String) {
    if (isMetricChartText(diagramText)) {
        val metrics = parseMetricItems(diagramText)
        if (metrics.isNotEmpty()) {
            GraphicalDataMetricCard(items = metrics)
            return
        }
    }

    if (isFlowchartText(diagramText)) {
        val nodes = parseFlowchartNodes(diagramText)
        if (nodes.size >= 2) {
            GraphicalFlowchartCard(nodes = nodes)
            return
        }
    }

    AsciiDiagramCard(diagramText = diagramText)
}

@Composable
fun GraphicalFlowchartCard(nodes: List<FlowNode>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = "Architecture & Flowchart",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "GRAPHICAL ARCHITECTURE & MENTAL FLOWCHART",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sequence of steps with directional connectors
            nodes.forEachIndexed { index, node ->
                val isUrdu = isUrduText(node.title)

                // Node Step Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Step number badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", node.stepIndex),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = if (isUrdu) 16.sp else 13.5.sp,
                                lineHeight = if (isUrdu) 28.sp else 18.sp,
                                textAlign = if (isUrdu) TextAlign.End else TextAlign.Start
                            )

                            if (!node.subtitle.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = node.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Default,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = if (isUrdu) 13.sp else 11.5.sp,
                                    lineHeight = if (isUrdu) 22.sp else 15.sp,
                                    textAlign = if (isUrdu) TextAlign.End else TextAlign.Start
                                )
                            }
                        }
                    }
                }

                // Connector Arrow (if not last)
                if (index < nodes.size - 1) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(12.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Next Phase",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GraphicalDataMetricCard(items: List<MetricItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Analytical Metrics",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "ANALYTICAL METRICS & DATA DISTRIBUTION",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            var chartMode by remember { mutableIntStateOf(0) } // 0: Bar, 1: Trend Curve

            // Matplotlib Chart Mode Selector & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartMode == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (chartMode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .clickable { chartMode = 0 }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "plt.bar",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (chartMode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartMode == 1) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (chartMode == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .clickable { chartMode = 1 }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "plt.plot",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (chartMode == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                // Matplotlib Academic Legend Box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Index", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.width(10.dp).height(2.dp).background(MaterialTheme.colorScheme.secondary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mean", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Matplotlib Scientific 2D Canvas Visualization
            if (items.isNotEmpty()) {
                val chartPrimary = MaterialTheme.colorScheme.primary
                val chartSecondary = MaterialTheme.colorScheme.secondary
                val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val yAxisX = 26f
                        val bottomBaseline = canvasHeight - 18f
                        val topMargin = 12f
                        val maxBarHeight = bottomBaseline - topMargin
                        val plotWidth = canvasWidth - yAxisX

                        // Draw Matplotlib Horizontal Dotted Gridlines & Ticks (0%, 25%, 50%, 75%, 100%)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        val gridLevels = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
                        gridLevels.forEach { level ->
                            val y = bottomBaseline - (level * maxBarHeight)
                            drawLine(
                                color = if (level == 0f) outlineColor else gridColor,
                                start = Offset(yAxisX, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = if (level == 0f) 1.5f else 1f,
                                pathEffect = if (level == 0f) null else dashEffect
                            )
                        }

                        // Y-Axis line
                        drawLine(
                            color = outlineColor,
                            start = Offset(yAxisX, topMargin),
                            end = Offset(yAxisX, bottomBaseline),
                            strokeWidth = 1.5f
                        )

                        val barCount = items.size
                        val slotWidth = plotWidth / barCount

                        if (chartMode == 0) {
                            // MODE 0: Matplotlib Bar Chart
                            val barWidth = (slotWidth * 0.55f).coerceAtMost(36f)
                            items.forEachIndexed { idx, item ->
                                val barHeight = (item.percentage * maxBarHeight).coerceAtLeast(4f)
                                val left = yAxisX + idx * slotWidth + (slotWidth - barWidth) / 2f
                                val top = bottomBaseline - barHeight

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(chartPrimary, chartSecondary)
                                    ),
                                    topLeft = Offset(left, top),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                            }
                        } else {
                            // MODE 1: Matplotlib Line Plot & Filled Area
                            if (items.size > 1) {
                                val path = Path()
                                val fillPath = Path()
                                val points = items.mapIndexed { idx, item ->
                                    val x = yAxisX + idx * slotWidth + slotWidth / 2f
                                    val y = bottomBaseline - (item.percentage * maxBarHeight)
                                    Offset(x, y)
                                }

                                path.moveTo(points.first().x, points.first().y)
                                fillPath.moveTo(points.first().x, bottomBaseline)
                                fillPath.lineTo(points.first().x, points.first().y)

                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val cX = (prev.x + curr.x) / 2f
                                    path.cubicTo(cX, prev.y, cX, curr.y, curr.x, curr.y)
                                    fillPath.cubicTo(cX, prev.y, cX, curr.y, curr.x, curr.y)
                                }

                                fillPath.lineTo(points.last().x, bottomBaseline)
                                fillPath.close()

                                // Area fill under curve
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(chartPrimary.copy(alpha = 0.35f), Color.Transparent),
                                        startY = topMargin,
                                        endY = bottomBaseline
                                    )
                                )

                                // Continuous curve line
                                drawPath(
                                    path = path,
                                    color = chartPrimary,
                                    style = Stroke(width = 3f)
                                )

                                // Data Node points
                                points.forEach { pt ->
                                    drawCircle(
                                        color = Color.White,
                                        radius = 5f,
                                        center = pt
                                    )
                                    drawCircle(
                                        color = chartSecondary,
                                        radius = 3.5f,
                                        center = pt
                                    )
                                }
                            }
                        }

                        // Reference Mean Line
                        val meanVal = items.map { it.percentage }.average().toFloat()
                        val meanY = bottomBaseline - (meanVal * maxBarHeight)
                        drawLine(
                            color = chartSecondary.copy(alpha = 0.7f),
                            start = Offset(yAxisX, meanY),
                            end = Offset(canvasWidth, meanY),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            items.forEach { item ->
                val isUrdu = isUrduText(item.label)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Default,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = if (isUrdu) 15.sp else 13.sp
                        )
                        Text(
                            text = item.displayValue,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { item.percentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Renders an ASCII Diagram or Architecture Flowchart inside an executive code card
 */
@Composable
private fun AsciiDiagramCard(diagramText: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "ARCHITECTURE & MENTAL MODEL FLOWCHART",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF090D16))
                    .padding(10.dp)
            ) {
                Text(
                    text = diagramText,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF67E8F9) // NeuroCyan Light
                )
            }
        }
    }
}

/**
 * Clickable markdown text with URL opening capabilities
 */
@Composable
private fun ClickableMarkdownLine(
    lineText: String,
    onSurface: Color,
    linkColor: Color,
    onOpenUrl: (String) -> Unit
) {
    val regex = Regex("\\[(.*?)\\]\\((https?://.*?)\\)")
    val matches = regex.findAll(lineText).toList()

    val annotatedString = buildAnnotatedString {
        var lastIdx = 0
        for (m in matches) {
            if (m.range.first > lastIdx) {
                append(lineText.substring(lastIdx, m.range.first))
            }

            val linkText = m.groups[1]?.value ?: "Link"
            val url = m.groups[2]?.value ?: ""

            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(
                SpanStyle(
                    color = linkColor,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(linkText)
            }
            pop()

            lastIdx = m.range.last + 1
        }
        if (lastIdx < lineText.length) {
            append(lineText.substring(lastIdx))
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = onSurface,
                fontSize = 14.sp,
                lineHeight = 21.sp
            ),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        onOpenUrl(annotation.item)
                    }
            }
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open Link",
            tint = linkColor,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ClickableUrlTile(
    url: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = url,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                maxLines = 1
            )
        }
    }
}

/**
 * Interactive Study Resources Card.
 * Gives students and professionals instant one-tap access to YouTube tutorials,
 * Wikipedia, official documentation, and academic papers for the topic.
 */
/**
 * Extracts a clean domain subject from note title and markdown content,
 * stripping boilerplate phrases like "Deep Research Dissertation:", "Master Study on", etc.
 */
fun extractCleanSubject(title: String, content: String): String {
    val candidate = if (title.isNotBlank() && title != "New Note" && title != "Audio Note" && title != "Note") {
        title
    } else {
        content.lines().firstOrNull { it.trim().startsWith("# ") }
            ?.removePrefix("# ")?.let { cleanAsterisks(it) } ?: ""
    }

    var clean = candidate.trim()
    val prefixes = listOf(
        "Deep Research Dissertation:",
        "Technical Architecture Blueprint:",
        "Executive Strategic Brief:",
        "Scientific & Clinical Dossier:",
        "Legal & Statutory Precedent:",
        "Standard Operating Playbook:",
        "Socratic Pedagogical Tutorial:",
        "Concept Tree & Mind Map:",
        "First-Principles Deconstruction:",
        "Expository & Narrative Essay:",
        "Custom User Specification:",
        "A Comprehensive Master Study on",
        "Comprehensive Master Study on",
        "Comprehensive Study on",
        "A Comprehensive Study on",
        "Deep Investigation into",
        "Investigation into",
        "Detailed Study on",
        "Analysis of",
        "Master Study:",
        "Discussion on",
        "Synthesis of",
        "Master Study on",
        "Audio Note:",
        "Audio Note"
    )
    for (p in prefixes) {
        if (clean.startsWith(p, ignoreCase = true)) {
            clean = clean.substring(p.length).trim()
        }
    }
    clean = clean.replace(Regex("[\"\'#*]"), "").trim()
    if (clean.endsWith(":")) clean = clean.dropLast(1).trim()
    return if (clean.isNotBlank()) clean else "Computer Science & Engineering"
}

/**
 * Interactive Study Resources Card.
 * Gives students and professionals instant one-tap access to YouTube tutorials,
 * Wikipedia, W3Schools, official documentation, and academic papers for the topic.
 */
@Composable
fun ExternalStudyResourcesCard(
    topic: String,
    onOpenUrl: (String) -> Unit
) {
    val isUrdu = isUrduText(topic)
    val encodedTopic = try {
        URLEncoder.encode(topic.trim(), "UTF-8")
    } catch (e: Exception) {
        topic.trim()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isUrdu) Arrangement.End else Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isUrdu) "مستند تعلیمی مآخذ و رہنمائی (Curated Educational Resources)" else "CLICKABLE STUDY RESOURCES & TUTORIALS",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isUrdu) 13.sp else 10.sp,
                    letterSpacing = if (isUrdu) 0.sp else 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verified Subject Focus Tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUrdu) "موضوع: $topic" else "Research Focus: $topic",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val topicLower = topic.lowercase()
            val isCoding = topicLower.contains("code") || topicLower.contains("python") || topicLower.contains("kotlin") || topicLower.contains("java") || topicLower.contains("sql") || topicLower.contains("git") || topicLower.contains("api") || topicLower.contains("algo") || topicLower.contains("software") || topicLower.contains("bug") || topicLower.contains("database") || topicLower.contains("security") || topicLower.contains("web") || topicLower.contains("docker") || topicLower.contains("injection") || topicLower.contains("script")
            val isMedical = topicLower.contains("medic") || topicLower.contains("patient") || topicLower.contains("clinic") || topicLower.contains("disease") || topicLower.contains("health") || topicLower.contains("symptom") || topicLower.contains("doctor") || topicLower.contains("drug") || topicLower.contains("cardiac") || topicLower.contains("neuro") || topicLower.contains("pharma") || topicLower.contains("syndrome") || topicLower.contains("cancer") || topicLower.contains("pathol")
            val isMathScience = topicLower.contains("math") || topicLower.contains("physics") || topicLower.contains("chem") || topicLower.contains("calculus") || topicLower.contains("algebra") || topicLower.contains("equation") || topicLower.contains("quantum") || topicLower.contains("formula") || topicLower.contains("mechanic") || topicLower.contains("gravity") || topicLower.contains("vector")
            val isBusiness = topicLower.contains("business") || topicLower.contains("finance") || topicLower.contains("market") || topicLower.contains("stock") || topicLower.contains("revenue") || topicLower.contains("profit") || topicLower.contains("invest") || topicLower.contains("startup") || topicLower.contains("manage") || topicLower.contains("sales") || topicLower.contains("econom") || topicLower.contains("bank")
            val isTeaching = topicLower.contains("teach") || topicLower.contains("lecture") || topicLower.contains("student") || topicLower.contains("exam") || topicLower.contains("course") || topicLower.contains("school") || topicLower.contains("college") || topicLower.contains("university") || topicLower.contains("syllabus") || topicLower.contains("textbook")

            // Dynamic Contextual Category Label
            val categoryLabel = when {
                isUrdu && isTeaching -> "متعلقہ تدریسی و امتحانی مآخذ (Curated Educational Resources)"
                isUrdu -> "مستند اردو علمی و وضاحتی مآخذ (Academic Study Resources)"
                isCoding -> "VERIFIED SOFTWARE & ENGINEERING DIRECTORY"
                isMedical -> "CLINICAL & BIOMEDICAL STUDY DIRECTORY"
                isMathScience -> "MATHEMATICS & PHYSICAL SCIENCES DIRECTORY"
                isBusiness -> "EXECUTIVE BUSINESS & FINANCIAL DIRECTORY"
                isTeaching -> "ACADEMIC LECTURES & CURRICULUM DIRECTORY"
                else -> "TARGETED STUDY RESOURCES & CITATIONS"
            }

            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = if (isUrdu) 12.sp else 9.5.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Resource Links Grid - Official Logos with Verified Check Badges and Generous Spacing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUrdu) {
                    if (isTeaching) {
                        ResourceActionChip(icon = Icons.Default.PlayCircle, label = "YouTube Urdu Lectures", accentColor = Color(0xFFEF4444), onClick = { onOpenUrl("https://www.youtube.com/results?search_query=$encodedTopic+لیکچر+اردو") })
                        ResourceActionChip(icon = Icons.Default.MenuBook, label = "Punjab Textbook Board", accentColor = Color(0xFF10B981), onClick = { onOpenUrl("https://pctb.punjab.gov.pk/") })
                        ResourceActionChip(icon = Icons.Default.School, label = "Urdu Wikipedia", accentColor = MaterialTheme.colorScheme.primary, onClick = { onOpenUrl("https://ur.wikipedia.org/wiki/Special:Search?search=$encodedTopic") })
                        ResourceActionChip(icon = Icons.Default.OpenInNew, label = "Rekhta Urdu Dictionary", accentColor = Color(0xFF8B5CF6), onClick = { onOpenUrl("https://www.rekhta.org/search?keyword=$encodedTopic") })
                        ResourceActionChip(icon = Icons.Default.School, label = "AIOU University", accentColor = Color(0xFF06B6D4), onClick = { onOpenUrl("https://www.google.com/search?q=$encodedTopic+AIOU+study+guide") })
                        ResourceActionChip(icon = Icons.Default.OpenInNew, label = "Urdu Scholar", accentColor = MaterialTheme.colorScheme.secondary, onClick = { onOpenUrl("https://scholar.google.com/scholar?q=$encodedTopic+Urdu") })
                    } else {
                        ResourceActionChip(icon = Icons.Default.PlayCircle, label = "YouTube Urdu", accentColor = Color(0xFFEF4444), onClick = { onOpenUrl("https://www.youtube.com/results?search_query=$encodedTopic+اردو") })
                        ResourceActionChip(icon = Icons.Default.MenuBook, label = "Urdu Wikipedia", accentColor = MaterialTheme.colorScheme.primary, onClick = { onOpenUrl("https://ur.wikipedia.org/wiki/Special:Search?search=$encodedTopic") })
                        ResourceActionChip(icon = Icons.Default.School, label = "Rekhta", accentColor = Color(0xFF8B5CF6), onClick = { onOpenUrl("https://www.rekhta.org/search?keyword=$encodedTopic") })
                        ResourceActionChip(icon = Icons.Default.OpenInNew, label = "Qaumi Council", accentColor = Color(0xFF06B6D4), onClick = { onOpenUrl("https://www.google.com/search?q=$encodedTopic+قومی+کونسل+برائے+فروغ+اردو") })
                        ResourceActionChip(icon = Icons.Default.OpenInNew, label = "Google Scholar Urdu", accentColor = MaterialTheme.colorScheme.secondary, onClick = { onOpenUrl("https://scholar.google.com/scholar?q=$encodedTopic+Urdu") })
                        ResourceActionChip(icon = Icons.Default.MenuBook, label = "Urdu Research Articles", accentColor = Color(0xFF10B981), onClick = { onOpenUrl("https://www.google.com/search?q=$encodedTopic+علمی+مضامین+اردو") })
                    }
                } else if (isCoding) {
                    ResourceActionChip(icon = Icons.Default.School, label = "W3Schools", accentColor = Color(0xFF04AA6D), onClick = { onOpenUrl("https://www.google.com/search?q=$encodedTopic+w3schools") })
                    ResourceActionChip(icon = Icons.Default.Code, label = "GitHub", accentColor = Color(0xFFF59E0B), onClick = { onOpenUrl("https://github.com/search?q=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.OpenInNew, label = "MDN Web Docs", accentColor = MaterialTheme.colorScheme.primary, onClick = { onOpenUrl("https://developer.mozilla.org/en-US/search?q=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.School, label = "StackOverflow", accentColor = Color(0xFFEA580C), onClick = { onOpenUrl("https://stackoverflow.com/search?q=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.Code, label = "LeetCode", accentColor = Color(0xFFEAB308), onClick = { onOpenUrl("https://www.google.com/search?q=$encodedTopic+leetcode") })
                    ResourceActionChip(icon = Icons.Default.PlayCircle, label = "YouTube Tutorials", accentColor = Color(0xFFEF4444), onClick = { onOpenUrl("https://www.youtube.com/results?search_query=$encodedTopic+tutorial") })
                } else if (isMedical) {
                    ResourceActionChip(icon = Icons.Default.School, label = "PubMed Central", accentColor = Color(0xFF10B981), onClick = { onOpenUrl("https://pubmed.ncbi.nlm.nih.gov/?term=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.OpenInNew, label = "WHO Global", accentColor = Color(0xFF06B6D4), onClick = { onOpenUrl("https://www.who.int/home/search?indexCatalogue=genericsearchindex1&searchQuery=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.MenuBook, label = "Medscape", accentColor = MaterialTheme.colorScheme.primary, onClick = { onOpenUrl("https://www.google.com/search?q=$encodedTopic+medscape") })
                    ResourceActionChip(icon = Icons.Default.School, label = "NCBI Books", accentColor = Color(0xFF8B5CF6), onClick = { onOpenUrl("https://www.ncbi.nlm.nih.gov/books/?term=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.MenuBook, label = "The Lancet", accentColor = Color(0xFFEA580C), onClick = { onOpenUrl("https://www.google.com/search?q=$encodedTopic+the+lancet") })
                    ResourceActionChip(icon = Icons.Default.PlayCircle, label = "Clinical Rounds", accentColor = Color(0xFFEF4444), onClick = { onOpenUrl("https://www.youtube.com/results?search_query=$encodedTopic+medical+lecture") })
                } else if (isMathScience) {
                    ResourceActionChip(icon = Icons.Default.School, label = "Khan Academy", accentColor = Color(0xFF10B981), onClick = { onOpenUrl("https://www.khanacademy.org/search?page_search_query=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.BarChart, label = "WolframAlpha", accentColor = Color(0xFFEF4444), onClick = { onOpenUrl("https://www.wolframalpha.com/input?i=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.PlayCircle, label = "3Blue1Brown MIT", accentColor = Color(0xFF8B5CF6), onClick = { onOpenUrl("https://www.youtube.com/results?search_query=$encodedTopic+3blue1brown+or+mit+ocw") })
                    ResourceActionChip(icon = Icons.Default.Code, label = "arXiv Science", accentColor = Color(0xFFF59E0B), onClick = { onOpenUrl("https://arxiv.org/search/?query=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.MenuBook, label = "Wikipedia", accentColor = MaterialTheme.colorScheme.primary, onClick = { onOpenUrl("https://en.wikipedia.org/wiki/Special:Search?search=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.School, label = "Google Scholar", accentColor = MaterialTheme.colorScheme.secondary, onClick = { onOpenUrl("https://scholar.google.com/scholar?q=$encodedTopic") })
                } else if (isBusiness) {
                    ResourceActionChip(icon = Icons.Default.MenuBook, label = "Investopedia", accentColor = Color(0xFF10B981), onClick = { onOpenUrl("https://www.investopedia.com/search?q=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.School, label = "Harvard Business Review", accentColor = Color(0xFFEF4444), onClick = { onOpenUrl("https://hbr.org/search?term=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.OpenInNew, label = "Bloomberg", accentColor = Color(0xFF06B6D4), onClick = { onOpenUrl("https://www.google.com/search?q=$encodedTopic+bloomberg") })
                    ResourceActionChip(icon = Icons.Default.PlayCircle, label = "Business Case Studies", accentColor = Color(0xFFF59E0B), onClick = { onOpenUrl("https://www.youtube.com/results?search_query=$encodedTopic+business+case+study") })
                    ResourceActionChip(icon = Icons.Default.School, label = "McKinsey", accentColor = MaterialTheme.colorScheme.primary, onClick = { onOpenUrl("https://www.google.com/search?q=$encodedTopic+mckinsey+insights") })
                    ResourceActionChip(icon = Icons.Default.School, label = "Scholar Economics", accentColor = MaterialTheme.colorScheme.secondary, onClick = { onOpenUrl("https://scholar.google.com/scholar?q=$encodedTopic+business") })
                } else {
                    ResourceActionChip(icon = Icons.Default.PlayCircle, label = "YouTube", accentColor = Color(0xFFEF4444), onClick = { onOpenUrl("https://www.youtube.com/results?search_query=$encodedTopic+overview") })
                    ResourceActionChip(icon = Icons.Default.MenuBook, label = "Wikipedia", accentColor = MaterialTheme.colorScheme.primary, onClick = { onOpenUrl("https://en.wikipedia.org/wiki/Special:Search?search=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.School, label = "Google Scholar", accentColor = MaterialTheme.colorScheme.secondary, onClick = { onOpenUrl("https://scholar.google.com/scholar?q=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.School, label = "MIT OpenCourseWare", accentColor = Color(0xFF06B6D4), onClick = { onOpenUrl("https://ocw.mit.edu/search/?q=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.Code, label = "arXiv Papers", accentColor = Color(0xFF8B5CF6), onClick = { onOpenUrl("https://arxiv.org/search/?query=$encodedTopic") })
                    ResourceActionChip(icon = Icons.Default.OpenInNew, label = "Official Standards", accentColor = MaterialTheme.colorScheme.secondary, onClick = { onOpenUrl("https://www.google.com/search?q=$encodedTopic+official+documentation") })
                }
            }
        }
    }
}

@Composable
private fun ResourceActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .border(1.2.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("res_badge_${label.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier.size(22.dp)
        )
        // Official Verified Check Badge in top right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(3.dp)
                .size(13.dp)
                .clip(CircleShape)
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Verified Resource",
                tint = Color.White,
                modifier = Modifier.size(9.dp)
            )
        }
    }
}

private fun openSafeUri(context: Context, uriHandler: androidx.compose.ui.platform.UriHandler, url: String) {
    try {
        val trimmed = url.trim()
        val formatted = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }
        uriHandler.openUri(formatted)
    } catch (e: Exception) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (ex: Exception) {
            Toast.makeText(context, "Could not open link: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

sealed class DocElement {
    data class Heading1(val text: String) : DocElement()
    data class Heading2(val text: String) : DocElement()
    data class Heading3(val text: String) : DocElement()
    data class Blockquote(val text: String) : DocElement()
    data class CodeBlock(val text: String) : DocElement()
    data class Table(val rows: List<List<String>>) : DocElement()
    data class CheckboxItem(val isChecked: Boolean, val text: String) : DocElement()
    data class BulletItem(val text: String) : DocElement()
    data class NumberedItem(val number: String, val text: String) : DocElement()
    data class LinkItem(val text: String) : DocElement()
    data class RawUrl(val url: String) : DocElement()
    object Divider : DocElement()
    data class Paragraph(val text: String) : DocElement()
}

fun parseContentToElements(content: String): List<DocElement> {
    val result = mutableListOf<DocElement>()
    val lines = content.lines()
    var inCodeBlock = false
    val codeBlockAccumulator = StringBuilder()
    var inTable = false
    val tableRows = mutableListOf<List<String>>()

    for (rawLine in lines) {
        val trimmed = rawLine.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                result.add(DocElement.CodeBlock(codeBlockAccumulator.toString().trimEnd()))
                codeBlockAccumulator.clear()
                inCodeBlock = false
            } else {
                inCodeBlock = true
            }
            continue
        }

        if (inCodeBlock) {
            codeBlockAccumulator.appendLine(rawLine)
            continue
        }

        // Table Detection
        if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
            // Skip markdown divider row like |---|---|
            val cleanSeparators = trimmed.replace("|", "").replace("-", "").replace(":", "").trim()
            if (cleanSeparators.isEmpty()) {
                continue
            }
            val cells = trimmed.split("|").filterIndexed { index, _ ->
                index > 0 && index < trimmed.split("|").size - 1
            }.map { it.trim() }

            tableRows.add(cells)
            inTable = true
            continue
        } else if (inTable) {
            if (tableRows.isNotEmpty()) {
                result.add(DocElement.Table(tableRows.toList()))
                tableRows.clear()
            }
            inTable = false
        }

        if (trimmed.isBlank()) continue

        when {
            trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                result.add(DocElement.Divider)
            }
            trimmed.startsWith("# ") -> {
                result.add(DocElement.Heading1(cleanAsterisks(trimmed.removePrefix("# "))))
            }
            trimmed.startsWith("## ") -> {
                result.add(DocElement.Heading2(cleanAsterisks(trimmed.removePrefix("## "))))
            }
            trimmed.startsWith("### ") -> {
                result.add(DocElement.Heading3(cleanAsterisks(trimmed.removePrefix("### "))))
            }
            trimmed.startsWith("> ") -> {
                result.add(DocElement.Blockquote(cleanAsterisks(trimmed.removePrefix("> "))))
            }
            trimmed.startsWith("- [ ]") || trimmed.startsWith("- [x]") -> {
                val isChecked = trimmed.startsWith("- [x]")
                result.add(DocElement.CheckboxItem(isChecked, trimmed.substring(5).trim()))
            }
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> {
                result.add(DocElement.RawUrl(trimmed))
            }
            trimmed.startsWith("- ") || trimmed.startsWith("• ") || trimmed.startsWith("* ") -> {
                val bulletText = trimmed.removePrefix("- ").removePrefix("• ").removePrefix("* ")
                result.add(DocElement.BulletItem(bulletText))
            }
            trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                val numPrefix = trimmed.substringBefore(" ")
                val itemText = trimmed.substringAfter(" ")
                result.add(DocElement.NumberedItem(numPrefix, itemText))
            }
            trimmed.contains(Regex("\\[(.*?)\\]\\((https?://.*?)\\)")) -> {
                result.add(DocElement.LinkItem(trimmed))
            }
            else -> {
                result.add(DocElement.Paragraph(trimmed))
            }
        }
    }

    if (inCodeBlock && codeBlockAccumulator.isNotEmpty()) {
        result.add(DocElement.CodeBlock(codeBlockAccumulator.toString().trimEnd()))
    }
    if (inTable && tableRows.isNotEmpty()) {
        result.add(DocElement.Table(tableRows.toList()))
    }

    return result
}

@Composable
private fun AcademicTableCard(rows: List<List<String>>) {
    if (rows.isEmpty()) return
    val header = rows.first()
    val bodyRows = rows.drop(1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            ) {
                for (cell in header) {
                    val isUrdu = isUrduText(cell)
                    Text(
                        text = cell.trim(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = if (isUrdu) 13.5.sp else 11.sp,
                        lineHeight = if (isUrdu) 22.sp else 16.sp,
                        textAlign = if (isUrdu) TextAlign.End else TextAlign.Start,
                        modifier = Modifier
                            .widthIn(min = 100.dp, max = 220.dp)
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            )

            // Body Rows
            bodyRows.forEachIndexed { idx, row ->
                Row(
                    modifier = Modifier
                        .background(if (idx % 2 == 1) MaterialTheme.colorScheme.surface.copy(alpha = 0.45f) else Color.Transparent)
                        .padding(vertical = 5.dp, horizontal = 4.dp)
                ) {
                    for (cell in row) {
                        val isUrdu = isUrduText(cell)
                        Text(
                            text = cell.trim(),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = if (isUrdu) UrduNastaliqFontFamily else FontFamily.Default,
                            fontSize = if (isUrdu) 13.sp else 12.sp,
                            lineHeight = if (isUrdu) 22.sp else 17.sp,
                            textAlign = if (isUrdu) TextAlign.End else TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .widthIn(min = 100.dp, max = 220.dp)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
