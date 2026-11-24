package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.ui.components.cleanAsterisks
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise Document Export & Share Manager.
 * Produces publication-grade, professionally aligned documents across
 * PDF print, Markdown (.md), clean text (.txt), and original voice audio (.m4a).
 * All documents use authoritative Times New Roman serif styling with executive branding.
 */
object ExportManager {

    /**
     * Alias for exportToPdfPrint for backward compatibility and clean API access.
     */
    fun exportNoteToPdf(context: Context, title: String, content: String) {
        exportToPdfPrint(context, title, content)
    }

    /**
     * Prints or saves document as a high-fidelity PDF using Android's native PrintManager
     * with Times New Roman typography, branded letterhead, and structured markdown parsing.
     */
    fun exportToPdfPrint(context: Context, title: String, content: String) {
        try {
            val cleanTitle = cleanAsterisks(title).ifBlank { "Cognitive Intelligence Document" }
            val currentDate = SimpleDateFormat("MMMM d, yyyy • HH:mm", Locale.getDefault()).format(Date())

            val htmlContent = buildHtmlDocument(cleanTitle, content, currentDate)

            val webView = WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                        val printAdapter = createPrintDocumentAdapter(cleanTitle)
                        printManager?.print(
                            cleanTitle,
                            printAdapter,
                            PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .build()
                        )
                    }
                }
            }

            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            Toast.makeText(context, "Opening PDF Print & Export...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("ExportManager", "Error generating PDF", e)
            Toast.makeText(context, "PDF export failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Converts markdown content into publication-grade HTML with Times New Roman typography,
     * bold spans, tables, diagrams, and section letterheads.
     */
    private fun buildHtmlDocument(title: String, rawContent: String, currentDate: String): String {
        return buildString {
            append("<!DOCTYPE html><html><head><meta charset='utf-8'>")
            append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            append("<style>")
            append("""
                @page {
                    size: A4 portrait;
                    margin: 18mm 18mm 20mm 18mm;
                }
                @media print {
                    body { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                    .no-break { page-break-inside: avoid; }
                    h2, h3 { page-break-after: avoid; }
                }
                * { box-sizing: border-box; }
                body {
                    font-family: 'Times New Roman', Times, 'Nimbus Roman No9 L', Georgia, serif;
                    color: #0f172a;
                    background: #ffffff;
                    line-height: 1.7;
                    padding: 10px 15px;
                    font-size: 14.5px;
                }
                .header-box {
                    border-bottom: 2.5px solid #0f172a;
                    padding-bottom: 14px;
                    margin-bottom: 22px;
                }
                .brand-tag {
                    font-family: 'Times New Roman', Times, serif;
                    font-size: 11px;
                    letter-spacing: 2.5px;
                    text-transform: uppercase;
                    color: #334155;
                    font-weight: 700;
                }
                h1.doc-title {
                    font-family: 'Times New Roman', Times, serif;
                    color: #0f172a;
                    font-size: 26px;
                    font-weight: 700;
                    margin: 8px 0 12px 0;
                    line-height: 1.25;
                }
                .meta-ribbon {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 12px;
                    font-size: 12px;
                    color: #475569;
                    font-family: 'Times New Roman', Times, serif;
                    font-style: italic;
                }
                .meta-badge {
                    background: #f8fafc;
                    padding: 3px 10px;
                    border-radius: 4px;
                    border: 1px solid #cbd5e1;
                }
                h2 {
                    font-family: 'Times New Roman', Times, serif;
                    color: #0f172a;
                    font-size: 19px;
                    font-weight: 700;
                    margin-top: 24px;
                    margin-bottom: 8px;
                    page-break-after: avoid;
                }
                h3 {
                    font-family: 'Times New Roman', Times, serif;
                    color: #1e293b;
                    font-size: 15.5px;
                    font-weight: 700;
                    margin-top: 18px;
                    margin-bottom: 6px;
                    page-break-after: avoid;
                }
                p {
                    margin: 10px 0;
                    color: #111827;
                    text-align: justify;
                    line-height: 1.7;
                }
                strong {
                    font-weight: 700;
                    color: #0f172a;
                }
                em {
                    font-style: italic;
                }
                code.inline-code {
                    background: #f1f5f9;
                    padding: 2px 6px;
                    border-radius: 3px;
                    font-family: 'Courier New', Courier, monospace;
                    font-size: 12.5px;
                    color: #0f172a;
                    border: 1px solid #e2e8f0;
                }
                ul, ol {
                    margin: 8px 0 14px 0;
                    padding-left: 26px;
                }
                li {
                    margin-bottom: 6px;
                    color: #111827;
                    line-height: 1.6;
                }
                blockquote {
                    background: #f8fafc;
                    border-left: 4px solid #0f172a;
                    margin: 14px 0;
                    padding: 12px 18px;
                    color: #334155;
                    font-size: 14px;
                    font-style: italic;
                    border-radius: 0 4px 4px 0;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 18px 0;
                    font-size: 13px;
                    font-family: 'Times New Roman', Times, serif;
                }
                th, td {
                    border: 1px solid #94a3b8;
                    padding: 9px 12px;
                    text-align: left;
                }
                th {
                    background-color: #f1f5f9;
                    font-weight: bold;
                    color: #0f172a;
                }
                pre {
                    background: #0f172a;
                    color: #38bdf8;
                    padding: 14px 16px;
                    border-radius: 4px;
                    font-family: 'Courier New', Courier, monospace;
                    font-size: 11.5px;
                    line-height: 1.5;
                    overflow-x: auto;
                    margin: 14px 0;
                }
                a {
                    color: #0369a1;
                    text-decoration: underline;
                    font-weight: 600;
                }
                .footer {
                    margin-top: 40px;
                    padding-top: 14px;
                    border-top: 1.5px solid #cbd5e1;
                    font-size: 11px;
                    font-family: 'Times New Roman', Times, serif;
                    font-style: italic;
                    color: #475569;
                    display: flex;
                    justify-content: space-between;
                }
                @import url('https://fonts.googleapis.com/css2?family=Noto+Nastaliq+Urdu:wght@400;500;600;700&family=Gulzar&display=swap');
                @font-face {
                    font-family: 'Jameel Noori Nastaleeq';
                    src: local('Jameel Noori Nastaleeq'), local('Nafees Nastaleeq'), local('Alvi Nastaleeq'), local('Urdu Typesetting');
                }
                .urdu-text {
                    font-family: 'Jameel Noori Nastaleeq', 'Noto Nastaliq Urdu', 'Gulzar', 'Nafees Nastaleeq', serif !important;
                    direction: rtl !important;
                    text-align: right !important;
                    line-height: 2.6 !important;
                    font-size: 17px !important;
                }
                h1.urdu-text, h2.urdu-text, h3.urdu-text {
                    font-family: 'Jameel Noori Nastaleeq', 'Noto Nastaliq Urdu', 'Gulzar', serif !important;
                    direction: rtl !important;
                    text-align: right !important;
                    line-height: 2.5 !important;
                    font-size: 21px !important;
                }
                th.urdu-text, td.urdu-text {
                    font-family: 'Jameel Noori Nastaleeq', 'Noto Nastaliq Urdu', 'Gulzar', serif !important;
                    direction: rtl !important;
                    text-align: right !important;
                    line-height: 2.2 !important;
                }
                .flowchart-box {
                    background: #f8fafc;
                    border: 1.5px solid #cbd5e1;
                    border-radius: 8px;
                    padding: 16px;
                    margin: 18px 0;
                }
                .flowchart-header {
                    font-size: 11px;
                    font-weight: 700;
                    color: #0369a1;
                    letter-spacing: 0.8px;
                    margin-bottom: 14px;
                    font-family: 'Courier New', Courier, monospace;
                }
                .flowchart-steps {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    gap: 6px;
                }
                .flowchart-card {
                    background: #ffffff;
                    border: 1.5px solid #0284c7;
                    border-radius: 6px;
                    padding: 10px 16px;
                    width: 85%;
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.06);
                }
                .step-badge {
                    background: #0284c7;
                    color: #ffffff;
                    font-weight: 700;
                    font-size: 11px;
                    border-radius: 50%;
                    width: 24px;
                    height: 24px;
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    flex-shrink: 0;
                }
                .step-title {
                    font-weight: 600;
                    color: #0f172a;
                    font-size: 13.5px;
                }
                .flowchart-arrow {
                    color: #0284c7;
                    font-size: 18px;
                    font-weight: 700;
                    line-height: 1;
                }
                .metric-box {
                    background: #f8fafc;
                    border: 1.5px solid #cbd5e1;
                    border-radius: 8px;
                    padding: 16px;
                    margin: 18px 0;
                }
                .metric-header {
                    font-size: 11px;
                    font-weight: 700;
                    color: #475569;
                    letter-spacing: 0.8px;
                    margin-bottom: 14px;
                    font-family: 'Courier New', Courier, monospace;
                }
                .metric-row {
                    margin-bottom: 12px;
                }
                .metric-labels {
                    display: flex;
                    justify-content: space-between;
                    font-size: 13px;
                    font-weight: 600;
                    color: #0f172a;
                    margin-bottom: 4px;
                }
                .metric-bar-bg {
                    background: #e2e8f0;
                    border-radius: 4px;
                    height: 8px;
                    overflow: hidden;
                }
                .metric-bar-fill {
                    background: #0284c7;
                    height: 100%;
                    border-radius: 4px;
                }
            """.trimIndent())
            append("</style></head><body>")

            // Executive Letterhead
            append("<div class='header-box'>")
            append("<div class='brand-tag'>EXECUTIVE MEMORANDUM & RESEARCH DOSSIER</div>")
            append("<h1 class='doc-title'>$title</h1>")
            append("<div class='meta-ribbon'>")
            append("<span class='meta-badge'>Archived: $currentDate</span>")
            append("<span class='meta-badge'>Synthesis: Multi-Dimensional Cognitive Masterwork</span>")
            append("<span class='meta-badge'>Standard: Academic Research Publication</span>")
            append("</div>")
            append("</div>")

            // Parse Markdown content without wiping out formatting markers first
            var inList = false
            var inPre = false
            val preBuffer = mutableListOf<String>()
            var inTable = false

            for (line in rawContent.lines()) {
                val trimmed = line.trim()

                if (trimmed.startsWith("```")) {
                    if (inList) { append("</ul>"); inList = false }
                    if (inTable) { append("</tbody></table>"); inTable = false }
                    if (inPre) {
                        append(formatCodeOrVisualHtml(preBuffer))
                        preBuffer.clear()
                        inPre = false
                    } else {
                        preBuffer.clear()
                        inPre = true
                    }
                    continue
                }

                if (inPre) {
                    preBuffer.add(line)
                    continue
                }

                // Table row parsing
                if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                    if (inList) { append("</ul>"); inList = false }
                    // Skip markdown separator row like |---|---|
                    if (trimmed.replace("|", "").replace("-", "").replace(":", "").trim().isEmpty()) {
                        continue
                    }
                    val rawCells = trimmed.split("|")
                    val cells = rawCells.subList(1, rawCells.size - 1)
                    if (!inTable) {
                        append("<table><thead><tr>")
                        for (cell in cells) {
                            val isUrdu = isUrduText(cell)
                            val cls = if (isUrdu) " class='urdu-text' dir='rtl'" else ""
                            append("<th$cls>${formatInlineMarkdown(cell.trim())}</th>")
                        }
                        append("</tr></thead><tbody>")
                        inTable = true
                    } else {
                        append("<tr>")
                        for (cell in cells) {
                            val isUrdu = isUrduText(cell)
                            val cls = if (isUrdu) " class='urdu-text' dir='rtl'" else ""
                            append("<td$cls>${formatInlineMarkdown(cell.trim())}</td>")
                        }
                        append("</tr>")
                    }
                    continue
                } else if (inTable) {
                    append("</tbody></table>")
                    inTable = false
                }

                when {
                    trimmed.startsWith("# ") -> {
                        if (inList) { append("</ul>"); inList = false }
                        val headingText = formatInlineMarkdown(trimmed.removePrefix("# "))
                        val isUrdu = isUrduText(headingText)
                        val cls = if (isUrdu) " class='urdu-text' dir='rtl'" else ""
                        if (!headingText.equals(title, ignoreCase = true)) {
                            append("<h2$cls>$headingText</h2>")
                        }
                    }
                    trimmed.startsWith("## ") -> {
                        if (inList) { append("</ul>"); inList = false }
                        val headingText = formatInlineMarkdown(trimmed.removePrefix("## "))
                        val isUrdu = isUrduText(headingText)
                        val cls = if (isUrdu) " class='urdu-text' dir='rtl'" else ""
                        append("<h2$cls>$headingText</h2>")
                    }
                    trimmed.startsWith("### ") -> {
                        if (inList) { append("</ul>"); inList = false }
                        val headingText = formatInlineMarkdown(trimmed.removePrefix("### "))
                        val isUrdu = isUrduText(headingText)
                        val cls = if (isUrdu) " class='urdu-text' dir='rtl'" else ""
                        append("<h3$cls>$headingText</h3>")
                    }
                    trimmed.startsWith("#### ") -> {
                        if (inList) { append("</ul>"); inList = false }
                        val headingText = formatInlineMarkdown(trimmed.removePrefix("#### "))
                        val isUrdu = isUrduText(headingText)
                        val cls = if (isUrdu) " class='urdu-text' dir='rtl'" else ""
                        append("<h3$cls style='font-size:14px;'>$headingText</h3>")
                    }
                    trimmed.startsWith("> ") -> {
                        if (inList) { append("</ul>"); inList = false }
                        val quoteText = formatInlineMarkdown(trimmed.removePrefix("> "))
                        val isUrdu = isUrduText(quoteText)
                        val cls = if (isUrdu) " class='urdu-text' dir='rtl'" else ""
                        append("<blockquote$cls>$quoteText</blockquote>")
                    }
                    trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                        if (!inList) { append("<ul>"); inList = true }
                        val itemText = formatInlineMarkdown(trimmed.drop(2).trim())
                        val isUrdu = isUrduText(itemText)
                        val cls = if (isUrdu) " class='urdu-text' dir='rtl'" else ""
                        append("<li$cls>$itemText</li>")
                    }
                    trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                        if (inList) { append("</ul>"); inList = false }
                        val itemText = formatInlineMarkdown(trimmed.replace(Regex("^\\d+\\.\\s*"), ""))
                        val isUrdu = isUrduText(itemText)
                        val cls = if (isUrdu) " class='urdu-text' dir='rtl'" else ""
                        append("<p$cls style='padding-left:14px; font-weight:500;'>${trimmed.takeWhile { it != '.' }}. $itemText</p>")
                    }
                    trimmed == "---" || trimmed == "***" -> {
                        if (inList) { append("</ul>"); inList = false }
                        append("<div style='height:14px;'></div>")
                    }
                    trimmed.isNotBlank() -> {
                        if (inList) { append("</ul>"); inList = false }
                        val bodyText = formatInlineMarkdown(trimmed)
                        val isUrdu = isUrduText(bodyText)
                        val cls = if (isUrdu) " class='urdu-text' dir='rtl'" else ""
                        append("<p$cls>$bodyText</p>")
                    }
                }
            }
            if (inList) append("</ul>")
            if (inTable) append("</tbody></table>")
            if (inPre) {
                append(formatCodeOrVisualHtml(preBuffer))
                preBuffer.clear()
            }

            append("<div class='footer'>")
            append("<span>Cognitive Intelligence & Document Synthesis</span>")
            append("<span>Dossier Reference ID: #${(System.currentTimeMillis() % 100000).toString().padStart(5, '0')} • Verified Publication</span>")
            append("</div>")
            append("</body></html>")
        }
    }

    private fun formatCodeOrVisualHtml(lines: List<String>): String {
        val fullText = lines.joinToString("\n")
        val trimmedLines = lines.map { it.trim() }.filter { it.isNotBlank() }

        // Check if metric chart
        val metricCount = trimmedLines.count { it.contains("%") || it.matches(Regex(".*:\\s*\\d+(\\.\\d+)?%?")) }
        if (metricCount >= 2 && metricCount >= (trimmedLines.size / 2)) {
            val sb = StringBuilder()
            sb.append("<div class='metric-box'>")
            sb.append("<div class='metric-header'>2D ANALYTICAL METRICS & DATA DISTRIBUTION</div>")
            val percentRegex = Regex("^(.*?):?\\s*(\\d+(?:\\.\\d+)?)\\s*%(.*)$")
            val parsedMetrics = mutableListOf<Pair<String, Float>>()
            for (line in trimmedLines) {
                val match = percentRegex.find(line)
                if (match != null) {
                    val label = match.groups[1]?.value?.trim()?.removePrefix("-")?.trim() ?: "Metric"
                    val num = match.groups[2]?.value?.toFloatOrNull() ?: 0f
                    parsedMetrics.add(label to num.coerceIn(0f, 100f))
                }
            }
            if (parsedMetrics.isNotEmpty()) {
                val svgHeight = 120
                sb.append("<svg viewBox='0 0 520 $svgHeight' width='100%' style='display:block; margin: 12px auto;'>")
                sb.append("<defs>")
                sb.append("<linearGradient id='barGrad' x1='0%' y1='0%' x2='0%' y2='100%'><stop offset='0%' stop-color='#4f46e5'/><stop offset='100%' stop-color='#06b6d4'/></linearGradient>")
                sb.append("<filter id='barShadow' x='-5%' y='-5%' width='110%' height='120%'><feDropShadow dx='0' dy='2' stdDeviation='2' flood-color='#0f172a' flood-opacity='0.1'/></filter>")
                sb.append("</defs>")
                sb.append("<line x1='20' y1='90' x2='500' y2='90' stroke='#e2e8f0' stroke-width='1.5'/>")
                val slot = 480f / parsedMetrics.size
                val barW = (slot * 0.48f).coerceAtMost(38f)
                parsedMetrics.forEachIndexed { i, pair ->
                    val maxH = 68f
                    val h = ((pair.second / 100f) * maxH).coerceAtLeast(6f)
                    val x = 20f + i * slot + (slot - barW) / 2f
                    val y = 90f - h
                    val isUrdu = isUrduText(pair.first)
                    val fontFam = if (isUrdu) "'Jameel Noori Nastaleeq', 'Noto Nastaliq Urdu', serif" else "'Helvetica Neue', Arial, sans-serif"
                    // Background track
                    sb.append("<rect x='$x' y='${90f - maxH}' width='$barW' height='$maxH' rx='4' ry='4' fill='#f1f5f9'/>")
                    // Foreground bar
                    sb.append("<rect x='$x' y='$y' width='$barW' height='$h' rx='4' ry='4' fill='url(#barGrad)' filter='url(#barShadow)'/>")
                    sb.append("<text x='${x + barW / 2f}' y='${y - 5}' font-size='10' font-weight='bold' fill='#4f46e5' text-anchor='middle' font-family='sans-serif'>${pair.second.toInt()}%</text>")
                    sb.append("<text x='${x + barW / 2f}' y='106' font-size='10.5' font-weight='600' fill='#334155' text-anchor='middle' font-family=\"$fontFam\">${escapeHtml(pair.first.take(16))}</text>")
                }
                sb.append("</svg>")
            }
            sb.append("</div>")
            return sb.toString()
        }

        // Check if flowchart
        val hasArrows = fullText.contains("->") || fullText.contains("-->") || fullText.contains("=>") || fullText.contains("↓")
        val hasSteps = fullText.contains("Step 1", ignoreCase = true) || fullText.contains("Phase 1", ignoreCase = true) || fullText.contains("+---")
        if (hasArrows || hasSteps) {
            val steps = mutableListOf<String>()
            if (fullText.contains("->") || fullText.contains("-->") || fullText.contains("=>")) {
                val raw = fullText.replace("-->", "->").replace("==>", "->").replace("=>", "->").split("->")
                    .map { it.replace("[", "").replace("]", "").replace("+", "").replace("|", "").trim() }
                    .filter { it.isNotBlank() && !it.all { ch -> ch == '-' || ch == '=' || ch == '>' || ch == '|' } }
                if (raw.size >= 2) {
                    steps.addAll(raw)
                }
            }
            if (steps.isEmpty()) {
                for (line in trimmedLines) {
                    val clean = line.removePrefix("|").removeSuffix("|").trim()
                    if (clean.isBlank() || clean == "v" || clean == "V" || clean == "↓" || clean.startsWith("+") || clean.all { it == '-' || it == '=' || it == '|' }) continue
                    steps.add(clean)
                }
            }

            if (steps.size >= 2) {
                val totalHeight = steps.size * 56 + 24
                val sb = StringBuilder()
                sb.append("<div class='flowchart-box'>")
                sb.append("<div class='flowchart-header'>2D GRAPHICAL ARCHITECTURE & MENTAL FLOWCHART</div>")
                sb.append("<svg viewBox='0 0 520 $totalHeight' width='100%' style='display:block; margin: 12px auto;'>")
                sb.append("<defs>")
                sb.append("<linearGradient id='cardGrad' x1='0%' y1='0%' x2='100%' y2='100%'><stop offset='0%' stop-color='#ffffff'/><stop offset='100%' stop-color='#f8fafc'/></linearGradient>")
                sb.append("<linearGradient id='stepBadge' x1='0%' y1='0%' x2='100%' y2='100%'><stop offset='0%' stop-color='#4f46e5'/><stop offset='100%' stop-color='#7c3aed'/></linearGradient>")
                sb.append("<filter id='cardShadow' x='-5%' y='-5%' width='110%' height='120%'><feDropShadow dx='0' dy='2' stdDeviation='2' flood-color='#0f172a' flood-opacity='0.08'/></filter>")
                sb.append("<marker id='arrow' viewBox='0 0 10 10' refX='6' refY='5' markerWidth='6' markerHeight='6' orient='auto-start-reverse'><path d='M 0 1 L 10 5 L 0 9 z' fill='#6366f1'/></marker>")
                sb.append("</defs>")
                steps.forEachIndexed { idx, step ->
                    val y = idx * 56 + 8
                    val isUrdu = isUrduText(step)
                    val fontFam = if (isUrdu) "'Jameel Noori Nastaleeq', 'Noto Nastaliq Urdu', serif" else "'Helvetica Neue', Arial, sans-serif"
                    // 2D Card with subtle shadow
                    sb.append("<rect x='30' y='$y' width='460' height='38' rx='8' ry='8' fill='url(#cardGrad)' stroke='#e2e8f0' stroke-width='1.2' filter='url(#cardShadow)'/>")
                    // Left accent bar
                    sb.append("<rect x='30' y='$y' width='5' height='38' rx='2.5' ry='2.5' fill='url(#stepBadge)'/>")
                    // Number Badge
                    sb.append("<circle cx='56' cy='${y + 19}' r='11' fill='url(#stepBadge)'/>")
                    sb.append("<text x='56' y='${y + 23}' font-size='9.5' font-weight='bold' fill='#ffffff' text-anchor='middle' font-family='sans-serif'>${String.format("%02d", idx + 1)}</text>")
                    sb.append("<text x='78' y='${y + 24}' font-size='12' font-weight='600' fill='#0f172a' font-family=\"$fontFam\">${escapeHtml(step.take(55))}</text>")
                    if (idx < steps.size - 1) {
                        sb.append("<line x1='260' y1='${y + 38}' x2='260' y2='${y + 56}' stroke='#6366f1' stroke-width='2' marker-end='url(#arrow)'/>")
                    }
                }
                sb.append("</svg>")
                sb.append("</div>")
                return sb.toString()
            }
        }

        // Default preformatted code
        val sb = StringBuilder("<pre>")
        for (line in lines) {
            sb.append(escapeHtml(line)).append("\n")
        }
        sb.append("</pre>")
        return sb.toString()
    }

    /**
     * Formats inline markdown elements:
     * - Bold+Italic (***text*** or **_text_**)
     * - Bold (**text**)
     * - Italic (*text* or _text_)
     * - Inline code (`code`)
     * - Hyperlinks ([text](url))
     */
    private fun formatInlineMarkdown(raw: String): String {
        var text = escapeHtml(raw)

        // Convert inline markdown links: [Title](url) -> <a href='url' target='_blank'>Title</a>
        text = text.replace(Regex("\\[(.*?)\\]\\((https?://.*?)\\)")) { match ->
            val linkText = match.groups[1]?.value ?: "Link"
            val linkUrl = match.groups[2]?.value ?: "#"
            "<a href='$linkUrl' target='_blank'>$linkText</a>"
        }

        // Bold + Italic: ***text***
        text = text.replace(Regex("\\*\\*\\*(.*?)\\*\\*\\*")) { "<strong><em>${it.groups[1]?.value}</em></strong>" }

        // Bold: **text**
        text = text.replace(Regex("\\*\\*(.*?)\\*\\*")) { "<strong>${it.groups[1]?.value}</strong>" }

        // Italic: *text* (when not preceded or followed by an asterisk)
        text = text.replace(Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)")) { "<em>${it.groups[1]?.value}</em>" }

        // Inline code: `code`
        text = text.replace(Regex("`([^`]+)`")) { "<code class='inline-code'>${it.groups[1]?.value}</code>" }

        return text
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    private fun isUrduText(text: String): Boolean {
        return text.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\uFB50'..'\uFDFF' || it in '\uFE70'..'\uFEFF' }
    }

    /**
     * Exports as Markdown (.md) or Text (.txt) file and launches the share sheet.
     */
    fun exportAndShareFile(context: Context, title: String, content: String, isMarkdown: Boolean) {
        try {
            val extension = if (isMarkdown) "md" else "txt"
            val sanitized = title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
            val fileName = "dossier_${sanitized}_${System.currentTimeMillis() % 10000}.$extension"

            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = File(exportDir, fileName)
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            val formattedExport = buildString {
                appendLine("================================================================================")
                appendLine("COGNITIVE INTELLIGENCE & RESEARCH ARCHIVE")
                appendLine("Title:     ${cleanAsterisks(title)}")
                appendLine("Exported:  $currentDate")
                appendLine("Type:      ${if (isMarkdown) "Markdown Structured Document (.md)" else "Plain Text Note (.txt)"}")
                appendLine("================================================================================")
                appendLine()
                appendLine(if (isMarkdown) content else cleanAsterisks(content))
                appendLine()
                appendLine("================================================================================")
                appendLine("End of Document")
                appendLine("================================================================================")
            }

            FileOutputStream(file).use { out ->
                out.write(formattedExport.toByteArray(Charsets.UTF_8))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (isMarkdown) "text/markdown" else "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title\n\n(See attached executive document)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Export $title (.$extension)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ExportManager", "File export error", e)
            sharePlainText(context, title, cleanAsterisks(content))
        }
    }

    /**
     * Plain text share via Android Share Sheet.
     */
    fun sharePlainText(context: Context, title: String, content: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "${cleanAsterisks(title)}\n\n${cleanAsterisks(content)}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(shareIntent, "Share Document").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }

    /**
     * Share original voice audio recording (.m4a).
     */
    fun shareAudioFile(context: Context, audioPath: String, title: String) {
        val file = File(audioPath)
        if (!file.exists()) {
            Toast.makeText(context, "Audio file not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Voice Recording: $title")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Audio Recording").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ExportManager", "Error sharing audio file", e)
            Toast.makeText(context, "Could not share audio: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Exports Active Recall Flashcards & Socratic Question Bank to PDF and shareable formats.
     */
    fun exportRetentionDossier(
        context: Context,
        flashcards: List<com.example.data.model.FlashcardEntity>,
        notes: List<com.example.data.model.NoteEntity>
    ) {
        val currentDate = SimpleDateFormat("MMMM d, yyyy • HH:mm", Locale.getDefault()).format(Date())
        val title = "Cognitive Retention & Socratic Recall Dossier"

        val markdownContent = buildString {
            appendLine("# $title")
            appendLine("> Ebbinghaus Spaced Repetition Continuum | Total Cards: ${flashcards.size} | Date: $currentDate")
            appendLine()
            appendLine("---")
            appendLine("## 1. Executive Retention Metrics")
            val masteredCount = flashcards.count { it.isMastered }
            val retentionRate = if (flashcards.isNotEmpty()) (masteredCount * 100) / flashcards.size else 100
            appendLine("- Total Active Flashcards: ${flashcards.size}")
            appendLine("- Mastered Concepts: $masteredCount")
            appendLine("- Consolidated Knowledge Quotient: $retentionRate%")
            appendLine("- Linked Topics & Cognitive Dossiers: ${notes.size}")
            appendLine()
            appendLine("---")
            appendLine("## 2. Active Recall Question Bank & Socratic Flashcards")
            appendLine()

            if (flashcards.isEmpty()) {
                appendLine("No active flashcards generated yet. Synthesize cognitive notes to automatically build your question bank.")
            } else {
                flashcards.forEachIndexed { idx, card ->
                    val num = idx + 1
                    val status = if (card.isMastered) "[MASTERED]" else "[ACTIVE REVIEW]"
                    appendLine("### Question $num: ${cleanAsterisks(card.question)}")
                    appendLine("- Concept Domain: ${cleanAsterisks(card.conceptKey)}")
                    appendLine("- Status: $status | Reviews Completed: ${card.recalledCount}")
                    appendLine("- Master Answer & Core Key:")
                    appendLine("  ${cleanAsterisks(card.answer)}")
                    appendLine()
                }
            }

            appendLine("---")
            appendLine("## 3. Spaced Repetition Scheduling Matrix")
            appendLine("| Item # | Concept Key | Reviews Completed | Mastery State |")
            appendLine("| --- | --- | --- | --- |")
            flashcards.take(15).forEachIndexed { idx, card ->
                val status = if (card.isMastered) "Consolidated" else "In Training"
                appendLine("| #${idx + 1} | ${cleanAsterisks(card.conceptKey)} | ${card.recalledCount} reviews | $status |")
            }
        }

        exportToPdfPrint(context, title, markdownContent)
    }
}
