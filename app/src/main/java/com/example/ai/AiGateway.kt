package com.example.ai

import com.example.audio.DetectedLanguage
import com.example.data.model.NoteEntity
import java.io.File

/**
 * Enterprise AI Gateway Abstraction (`AiGateway`).
 *
 * Allows swapping the underlying transport between:
 * 1. A Cloud Run / Cloud Function proxy secured with Firebase App Check and per-user rate limiting,
 * 2. A user-supplied API key stored in Android Keystore / EncryptedSharedPreferences, or
 * 3. Direct Gemini Cognitive Service with local degraded offline fallback.
 */
interface AiGateway {
    suspend fun transcribeAudio(
        audioFile: File,
        selectedLang: DetectedLanguage,
        audioMimeType: String? = null
    ): String

    suspend fun synthesizeCapture(
        rawTranscript: String,
        workflowMode: String,
        depthMode: CognitiveDepthMode,
        language: DetectedLanguage,
        formatArchetype: CognitiveFormatArchetype,
        customInstructions: String = ""
    ): CognitiveSynthesisResult

    suspend fun askVault(
        query: String,
        mode: AskNeuroScribeMode,
        candidateNotes: List<NoteEntity>
    ): AskNeuroScribeResult
}

/**
 * Typed Document Block Contract.
 * Ensures UI renders structured semantic blocks without raw markdown or asterisks.
 */
sealed class DocumentBlock {
    data class Heading(val level: Int, val text: String) : DocumentBlock()
    data class Paragraph(val text: String) : DocumentBlock()
    data class BulletList(val items: List<String>) : DocumentBlock()
    data class NumberedList(val items: List<String>) : DocumentBlock()
    data class Callout(val text: String, val isDisclaimer: Boolean = false) : DocumentBlock()
    data class LinkBlock(val label: String, val url: String) : DocumentBlock()
}
