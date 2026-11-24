package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.CognitiveDepthMode
import com.example.ai.CognitiveFormatArchetype
import com.example.ai.CognitiveSynthesisResult
import com.example.ai.GeminiCognitiveService
import com.example.ai.RefinementType
import com.example.audio.AudioPlayerManager
import com.example.audio.AudioRecorderManager
import com.example.audio.AudioRecordingResult
import com.example.audio.AuditoryFilterState
import com.example.audio.DetectedLanguage
import com.example.audio.SpeechRecognitionManager
import com.example.audio.VoiceActivityFilter
import com.example.ai.AskNeuroScribeMode
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.KnowledgeConnectionEntity
import com.example.data.model.NoteEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.StudyPlanEntity
import com.example.data.repository.NoteRepository
import com.example.util.SpacedRepetitionScheduler
import com.example.ui.components.PipelineStage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    HOME,
    CAPTURE,
    KNOWLEDGE,
    ASK_AI,
    LIBRARY,
    RETENTION,
    DETAIL,
    ARCHIVE,
    MEDIA_LIBRARY
}

enum class MediaType { AUDIO, IMAGE }

data class MediaItem(
    val id: String,
    val type: MediaType,
    val title: String,
    val filePath: String,
    val durationMs: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val noteId: Long? = null,
    val noteTitle: String? = null
)

enum class WorkflowMode(val label: String, val shortDesc: String) {
    WORKFLOW_3_COMBO_MASTERWORK("Combo Archive", "Raw Audio + Verbatim Transcript + Deep Cognitive Masterwork"),
    WORKFLOW_2_MIND_SYNTHESIS("Cognitive Masterwork", "Deep Multidimensional Research Study Guide"),
    WORKFLOW_1_RAW_STREAM("Verbatim Audio", "Word-for-Word Transcription & Audio Log")
}

sealed interface AiProcessingState {
    data object Idle : AiProcessingState
    data class Processing(val stageMessage: String) : AiProcessingState
    data class Success(val message: String) : AiProcessingState
    data class Error(val message: String) : AiProcessingState
}

data class NoSpeechDetectedInfo(
    val audioPath: String?,
    val audioDurationMs: Long,
    val recordingResult: AudioRecordingResult?,
    val isImported: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication<Application>().applicationContext
    private val database = AppDatabase.getInstance(context)
    private val repository = NoteRepository(database)

    val recorderManager = AudioRecorderManager(context)
    val playerManager = AudioPlayerManager()
    val speechManager = SpeechRecognitionManager(context)
    val geminiService = GeminiCognitiveService()

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.CAPTURE)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote.asStateFlow()

    // Theme Mode (Dark by default, toggleable)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Floating Action Side Dock - disabled by default, user can enable in Settings
    private val _isSideDockEnabled = MutableStateFlow(false)
    val isSideDockEnabled: StateFlow<Boolean> = _isSideDockEnabled.asStateFlow()

    fun toggleSideDock() {
        val newState = !_isSideDockEnabled.value
        _isSideDockEnabled.value = newState
        if (!newState) {
            try {
                val app = getApplication<Application>()
                val intent = Intent(app, com.example.service.FloatingOverlayService::class.java)
                app.stopService(intent)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error stopping FloatingOverlayService", e)
            }
        }
    }

    // Camera Trigger State (for instant launch from side dock or external overlay)
    private val _pendingCameraTrigger = MutableStateFlow(false)
    val pendingCameraTrigger: StateFlow<Boolean> = _pendingCameraTrigger.asStateFlow()

    fun triggerCameraLaunch() {
        _pendingCameraTrigger.value = true
        navigateTo(AppScreen.CAPTURE)
    }

    fun clearCameraTrigger() {
        _pendingCameraTrigger.value = false
    }

    // Most recent saved note (so user sees their saved recording right on Capture screen!)
    private val _latestSavedNote = MutableStateFlow<NoteEntity?>(null)
    val latestSavedNote: StateFlow<NoteEntity?> = _latestSavedNote.asStateFlow()

    // Capture & Filtration Configuration
    private val _workflowMode = MutableStateFlow(WorkflowMode.WORKFLOW_3_COMBO_MASTERWORK)
    val workflowMode: StateFlow<WorkflowMode> = _workflowMode.asStateFlow()

    private val _cognitiveDepthMode = MutableStateFlow(CognitiveDepthMode.COMPREHENSIVE_MASTERWORK)
    val cognitiveDepthMode: StateFlow<CognitiveDepthMode> = _cognitiveDepthMode.asStateFlow()

    // Pause/Resume recording state from AudioRecorderManager
    val isPaused: StateFlow<Boolean> = recorderManager.isPaused

    private val _selectedFormatArchetype = MutableStateFlow(CognitiveFormatArchetype.RESEARCH_DISSERTATION)
    val selectedFormatArchetype: StateFlow<CognitiveFormatArchetype> = _selectedFormatArchetype.asStateFlow()

    private val _customPromptInstructions = MutableStateFlow("")
    val customPromptInstructions: StateFlow<String> = _customPromptInstructions.asStateFlow()

    fun setFormatArchetype(archetype: CognitiveFormatArchetype) {
        _selectedFormatArchetype.value = archetype
    }

    fun setCustomPromptInstructions(instructions: String) {
        _customPromptInstructions.value = instructions
    }

    fun pauseRecording() {
        recorderManager.pauseRecording()
    }

    fun resumeRecording() {
        recorderManager.resumeRecording()
    }

    fun setCognitiveDepthMode(mode: CognitiveDepthMode) {
        _cognitiveDepthMode.value = mode
    }

    fun resetCaptureSession() {
        if (recorderManager.isRecording.value) {
            recorderManager.stopRecording()
        }
        playerManager.stop()
        speechManager.clearTranscript()
        _currentAudioPath.value = null
        _pipelineStage.value = PipelineStage.IDLE
        _pipelineDetail.value = ""
        _lastSavedResult.value = null
        _noSpeechDetectedState.value = null
        _aiState.value = AiProcessingState.Idle
    }

    private val _selectedLanguage = MutableStateFlow(DetectedLanguage.AUTO)
    val selectedLanguage: StateFlow<DetectedLanguage> = _selectedLanguage.asStateFlow()

    private val _currentAudioPath = MutableStateFlow<String?>(null)
    val currentAudioPath: StateFlow<String?> = _currentAudioPath.asStateFlow()

    private val _aiState = MutableStateFlow<AiProcessingState>(AiProcessingState.Idle)
    val aiState: StateFlow<AiProcessingState> = _aiState.asStateFlow()

    // Progressive Pipeline Lazy-Loading State
    private val _pipelineStage = MutableStateFlow(PipelineStage.IDLE)
    val pipelineStage: StateFlow<PipelineStage> = _pipelineStage.asStateFlow()

    private val _pipelineDetail = MutableStateFlow("")
    val pipelineDetail: StateFlow<String> = _pipelineDetail.asStateFlow()

    private val _lastSavedResult = MutableStateFlow<AudioRecordingResult?>(null)
    val lastSavedResult: StateFlow<AudioRecordingResult?> = _lastSavedResult.asStateFlow()

    // No Speech Detected prompt state for manual transcription or import
    private val _noSpeechDetectedState = MutableStateFlow<NoSpeechDetectedInfo?>(null)
    val noSpeechDetectedState: StateFlow<NoSpeechDetectedInfo?> = _noSpeechDetectedState.asStateFlow()

    fun clearNoSpeechDetected() {
        _noSpeechDetectedState.value = null
    }

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _archiveTab = MutableStateFlow(0) // 0: All, 1: Sensory Buffer, 2: Long-Term Memory
    val archiveTab: StateFlow<Int> = _archiveTab.asStateFlow()

    // Database Flows
    val allNotes: StateFlow<List<NoteEntity>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFlashcards: StateFlow<List<FlashcardEntity>> = repository.getAllFlashcards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueFlashcards: StateFlow<List<FlashcardEntity>> = repository.getDueFlashcards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKnowledgeConnections: StateFlow<List<KnowledgeConnectionEntity>> = repository.getAllKnowledgeConnections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizzes: StateFlow<List<QuizEntity>> = repository.getAllQuizzes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudyPlans: StateFlow<List<StudyPlanEntity>> = repository.getAllStudyPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.getChatMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Quiz State
    private val _activeQuiz = MutableStateFlow<QuizEntity?>(null)
    val activeQuiz: StateFlow<QuizEntity?> = _activeQuiz.asStateFlow()

    private val _activeQuizQuestions = MutableStateFlow<List<QuizQuestionEntity>>(emptyList())
    val activeQuizQuestions: StateFlow<List<QuizQuestionEntity>> = _activeQuizQuestions.asStateFlow()

    // Ask AI State
    private val _askAiState = MutableStateFlow<AiProcessingState>(AiProcessingState.Idle)
    val askAiState: StateFlow<AiProcessingState> = _askAiState.asStateFlow()

    val filteredNotes: StateFlow<List<NoteEntity>> = combine(
        allNotes,
        _searchQuery,
        _selectedCategory,
        _archiveTab
    ) { notes, query, category, tab ->
        notes.filter { note ->
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.rawTranscript.contains(query, ignoreCase = true) ||
                    note.synthesizedContent.contains(query, ignoreCase = true) ||
                    note.tags.contains(query, ignoreCase = true)

            val matchesCategory = (category == "All") || note.category.equals(category, ignoreCase = true)

            val matchesTab = when (tab) {
                1 -> !note.isLongTermMemory // Sensory / Short-Term Buffer
                2 -> note.isLongTermMemory // Long-Term Retained
                else -> true
            }

            matchesQuery && matchesCategory && matchesTab
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unified Media Library Flow (Audio recordings + Visual captures across notes and storage)
    val mediaItems: StateFlow<List<MediaItem>> = allNotes.map { notes ->
        val list = mutableListOf<MediaItem>()
        notes.forEach { note ->
            if (!note.audioFilePath.isNullOrBlank()) {
                val f = File(note.audioFilePath)
                val exists = f.exists()
                val size = if (exists) f.length() else 0L
                list.add(
                    MediaItem(
                        id = "audio_${note.id}",
                        type = MediaType.AUDIO,
                        title = note.title.ifBlank { "Voice Note #${note.id}" },
                        filePath = note.audioFilePath,
                        durationMs = note.audioDurationMs,
                        fileSizeBytes = size,
                        timestamp = note.createdAt,
                        noteId = note.id,
                        noteTitle = note.title
                    )
                )
            }
            if (!note.imageFilePath.isNullOrBlank()) {
                val f = File(note.imageFilePath)
                val exists = f.exists()
                val size = if (exists) f.length() else 0L
                list.add(
                    MediaItem(
                        id = "image_${note.id}",
                        type = MediaType.IMAGE,
                        title = "Visual: ${note.title}",
                        filePath = note.imageFilePath,
                        fileSizeBytes = size,
                        timestamp = note.createdAt,
                        noteId = note.id,
                        noteTitle = note.title
                    )
                )
            }
        }
        list.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playMediaAudio(filePath: String) {
        playerManager.playOrPause(filePath)
    }

    fun stopMediaAudio() {
        playerManager.stop()
    }

    fun openNoteForMedia(noteId: Long) {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            if (note != null) {
                selectNote(note)
                navigateTo(AppScreen.DETAIL)
            }
        }
    }

    fun deleteMediaItem(item: MediaItem) {
        viewModelScope.launch {
            try {
                if (playerManager.currentPlayingPath.value == item.filePath) {
                    playerManager.stop()
                }
                val file = File(item.filePath)
                if (file.exists()) file.delete()

                item.noteId?.let { nId ->
                    val note = repository.getNoteById(nId)
                    if (note != null) {
                        val updated = if (item.type == MediaType.AUDIO) {
                            note.copy(audioFilePath = null, audioDurationMs = 0L)
                        } else {
                            note.copy(imageFilePath = null)
                        }
                        repository.updateNote(updated)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error deleting media item", e)
            }
        }
    }

    // Auditory state combined from amplitude & speech recognition
    val auditoryFilterState: StateFlow<AuditoryFilterState> = combine(
        recorderManager.amplitude,
        speechManager.partialTranscript,
        speechManager.finalTranscript
    ) { amp, partial, final ->
        val hasText = partial.isNotBlank() || final.isNotBlank()
        VoiceActivityFilter.evaluateAudioState(amp, hasText)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuditoryFilterState.IDLE)

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectNote(note: NoteEntity) {
        _selectedNote.value = note
        _currentScreen.value = AppScreen.DETAIL
    }

    fun setWorkflowMode(mode: WorkflowMode) {
        _workflowMode.value = mode
    }

    fun setSelectedLanguage(lang: DetectedLanguage) {
        _selectedLanguage.value = lang
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setArchiveTab(tabIndex: Int) {
        _archiveTab.value = tabIndex
    }

    fun startListeningAndRecording() {
        _currentAudioPath.value = null
        speechManager.clearTranscript()

        _pipelineStage.value = PipelineStage.STAGE_1_CAPTURE
        _pipelineDetail.value = "Auditory sensor active: Ingesting speech in ${_selectedLanguage.value.label}..."

        val filePath = recorderManager.startRecording()
        _currentAudioPath.value = filePath
        speechManager.startListening(_selectedLanguage.value)
    }

    fun stopListeningAndRecording() {
        val recordingResult = recorderManager.stopRecording()
        _lastSavedResult.value = recordingResult
        _currentAudioPath.value = recordingResult?.filePath
        speechManager.stopListening()

        val rawSpokenText = speechManager.finalTranscript.value.ifBlank {
            speechManager.partialTranscript.value
        }.trim()

        processAndSaveCurrentCapture(
            initialTranscript = rawSpokenText,
            audioPath = recordingResult?.filePath,
            audioDurationMs = recordingResult?.durationMs ?: 1500L,
            recordingResult = recordingResult
        )
    }

    fun importAudioFile(uri: android.net.Uri, context: Context) {
        viewModelScope.launch {
            try {
                _pipelineStage.value = PipelineStage.STAGE_1_CAPTURE
                _pipelineDetail.value = "Reading audio file from storage..."

                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: "audio/mp4"

                var fileName = "imported_voice_${System.currentTimeMillis()}.m4a"
                var fileSize = 0L

                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIdx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (nameIdx != -1) {
                            it.getString(nameIdx)?.let { name -> fileName = name }
                        }
                        if (sizeIdx != -1) {
                            fileSize = it.getLong(sizeIdx)
                        }
                    }
                }

                val audioDir = File(context.filesDir, "audio_notes")
                if (!audioDir.exists()) audioDir.mkdirs()
                val targetFile = File(audioDir, fileName)

                contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (fileSize == 0L) fileSize = targetFile.length()

                var durationMs = 3000L
                try {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(targetFile.absolutePath)
                    val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    durationMs = durStr?.toLongOrNull() ?: 3000L
                    retriever.release()
                } catch (e: Exception) {
                    durationMs = 3000L
                }

                val recResult = AudioRecordingResult(
                    filePath = targetFile.absolutePath,
                    durationMs = durationMs,
                    fileSizeBytes = fileSize,
                    fileName = fileName
                )
                _lastSavedResult.value = recResult
                _currentAudioPath.value = targetFile.absolutePath

                _pipelineStage.value = PipelineStage.STAGE_2_ENCODE
                _pipelineDetail.value = "Ingested $fileName (${fileSize / 1024} KB) • Preparing transcription..."
                delay(350)

                _pipelineStage.value = PipelineStage.STAGE_3_TRANSCRIBE
                _pipelineDetail.value = "Transcribing spoken voice in ${_selectedLanguage.value.label}..."

                val transcribed = geminiService.transcribeAudioFile(targetFile, _selectedLanguage.value, mimeType)
                speechManager.setTranscript(transcribed)

                processAndSaveCurrentCapture(
                    initialTranscript = transcribed,
                    audioPath = targetFile.absolutePath,
                    audioDurationMs = durationMs,
                    recordingResult = recResult
                )
            } catch (e: Exception) {
                Log.e("MainViewModel", "Audio import failed", e)
                _pipelineStage.value = PipelineStage.IDLE
                _aiState.value = AiProcessingState.Error("Audio import failed: ${e.localizedMessage}")
            }
        }
    }

    fun createTempCameraImageFile(context: Context): Pair<File, android.net.Uri> {
        val photoDir = File(context.cacheDir, "camera_photos")
        if (!photoDir.exists()) photoDir.mkdirs()
        val file = File(photoDir, "neuro_photo_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.provider"
        val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
        return Pair(file, uri)
    }

    fun processCapturedImage(file: File, mimeType: String = "image/jpeg") {
        viewModelScope.launch {
            try {
                _pipelineStage.value = PipelineStage.STAGE_1_CAPTURE
                _pipelineDetail.value = "Visual sensor active: Ingesting image (${file.length() / 1024} KB)..."
                delay(250)

                _pipelineStage.value = PipelineStage.STAGE_2_ENCODE
                _pipelineDetail.value = "Analyzing visual segments & encoding..."
                delay(250)

                _pipelineStage.value = PipelineStage.STAGE_3_TRANSCRIBE
                _pipelineDetail.value = "Neural OCR: Transcribing visible text, code & diagrams..."

                val workflowStr = when (_workflowMode.value) {
                    WorkflowMode.WORKFLOW_3_COMBO_MASTERWORK -> "COMBO_MASTERWORK"
                    WorkflowMode.WORKFLOW_2_MIND_SYNTHESIS -> "COGNITIVE_SYNTHESIS"
                    WorkflowMode.WORKFLOW_1_RAW_STREAM -> "RAW_STREAM"
                }
                val result = geminiService.processVisualCapture(
                    imageFile = file,
                    mimeType = mimeType,
                    workflowMode = workflowStr,
                    depthMode = _cognitiveDepthMode.value,
                    language = _selectedLanguage.value
                )

                _pipelineStage.value = PipelineStage.STAGE_4_SYNTHESIS
                _pipelineDetail.value = "Synthesizing research masterwork from visual data..."
                delay(300)

                val noteEntity = NoteEntity(
                    title = result.title,
                    rawTranscript = result.extractedTranscript.ifBlank { result.summary },
                    synthesizedContent = result.structuredContent,
                    workflowType = workflowStr,
                    language = result.detectedLanguage,
                    audioFilePath = null,
                    audioDurationMs = 0L,
                    imageFilePath = file.absolutePath,
                    category = result.keyTerms.firstOrNull() ?: "Visual Research",
                    tags = result.keyTerms.joinToString(", "),
                    summary = result.summary,
                    keyTerms = result.keyTerms.joinToString(", "),
                    actionItems = result.actionItems.joinToString("\n") { "• $it" },
                    createdAt = System.currentTimeMillis()
                )

                val newId = repository.insertNote(noteEntity)
                val savedNote = noteEntity.copy(id = newId)

                // Insert flashcards
                if (result.flashcards.isNotEmpty()) {
                    val cards = result.flashcards.map { card ->
                        FlashcardEntity(
                            noteId = newId,
                            question = card.question,
                            answer = card.answer,
                            conceptKey = card.conceptKey
                        )
                    }
                    repository.insertFlashcards(cards)
                }

                _latestSavedNote.value = savedNote
                _pipelineStage.value = PipelineStage.STAGE_5_ARCHIVED
                _pipelineDetail.value = "Visual intelligence synthesized & archived: ${savedNote.title}"
                speechManager.setTranscript(result.extractedTranscript)

            } catch (e: Exception) {
                Log.e("MainViewModel", "Visual capture processing failed", e)
                _pipelineStage.value = PipelineStage.IDLE
                _aiState.value = AiProcessingState.Error("Visual processing failed: ${e.localizedMessage}")
            }
        }
    }

    fun importImageFile(uri: android.net.Uri, context: Context) {
        viewModelScope.launch {
            try {
                _pipelineStage.value = PipelineStage.STAGE_1_CAPTURE
                _pipelineDetail.value = "Reading document/image from storage..."

                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val photoDir = File(context.cacheDir, "camera_photos")
                if (!photoDir.exists()) photoDir.mkdirs()

                val ext = if (mimeType.contains("png", true)) "png" else "jpg"
                val targetFile = File(photoDir, "imported_image_${System.currentTimeMillis()}.$ext")

                contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                processCapturedImage(targetFile, mimeType)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Image import failed", e)
                _pipelineStage.value = PipelineStage.IDLE
                _aiState.value = AiProcessingState.Error("Image import failed: ${e.localizedMessage}")
            }
        }
    }

    fun processAndSaveCurrentCapture(
        initialTranscript: String,
        audioPath: String? = _currentAudioPath.value,
        audioDurationMs: Long = recorderManager.recordingDurationMs.value,
        recordingResult: AudioRecordingResult? = null
    ) {
        viewModelScope.launch {
            try {
                // Step 2: Audio File Encoding
                _pipelineStage.value = PipelineStage.STAGE_2_ENCODE
                val sizeStr = if (recordingResult != null) "${recordingResult.fileSizeBytes / 1024} KB" else "Saved"
                val fName = recordingResult?.fileName ?: "neuro_voice_${System.currentTimeMillis() % 10000}.m4a"
                _pipelineDetail.value = "Encoded audio: $fName ($sizeStr) in internal storage."
                delay(300)

                // Step 3: High-Precision Transcription (Speech-to-Text)
                _pipelineStage.value = PipelineStage.STAGE_3_TRANSCRIBE
                _pipelineDetail.value = "Transcribing voice in ${_selectedLanguage.value.label}..."

                var effectiveTranscript = initialTranscript.trim()

                // Transcribe the actual recorded or imported audio file with Gemini multimodal engine
                val audioFile = audioPath?.let { File(it) }
                if (audioFile != null && audioFile.exists() && audioFile.length() > 200) {
                    _pipelineDetail.value = "Transcribing spoken speech verbatim..."
                    val fileTranscript = geminiService.transcribeAudioFile(audioFile, _selectedLanguage.value)
                    if (fileTranscript.isNotBlank() && !fileTranscript.startsWith("Spoken voice session") && !fileTranscript.startsWith("[Audio recording")) {
                        effectiveTranscript = fileTranscript
                        speechManager.setTranscript(fileTranscript)
                    } else if (effectiveTranscript.isNotBlank()) {
                        // Keep live continuous speech transcript
                        speechManager.setTranscript(effectiveTranscript)
                    } else {
                        // High quality audio capture fallback
                        val durSec = (audioDurationMs / 1000L).coerceAtLeast(1L)
                        effectiveTranscript = "Voice Session (${durSec}s) captured in ${_selectedLanguage.value.label}. Acoustic waveform securely archived with high-definition voice compression."
                        speechManager.setTranscript(effectiveTranscript)
                    }
                } else if (effectiveTranscript.isBlank()) {
                    val durSec = (audioDurationMs / 1000L).coerceAtLeast(1L)
                    effectiveTranscript = "Voice Session (${durSec}s) captured. Acoustic audio stream archived."
                    speechManager.setTranscript(effectiveTranscript)
                }

                delay(300)

                // Step 4: Cognitive & NLP Synthesis (Workflow 1 vs Workflow 2)
                _pipelineStage.value = PipelineStage.STAGE_4_SYNTHESIS
                _pipelineDetail.value = if (_workflowMode.value == WorkflowMode.WORKFLOW_1_RAW_STREAM) {
                    "Extracting raw NLP terms & structuring verbatim note..."
                } else {
                    "Synthesizing cognitive concepts, glossary & retention cards..."
                }

                val detectedLangEnum = if (_selectedLanguage.value == DetectedLanguage.AUTO) {
                    VoiceActivityFilter.detectLanguage(effectiveTranscript)
                } else {
                    _selectedLanguage.value
                }

                val result: CognitiveSynthesisResult = when (_workflowMode.value) {
                    WorkflowMode.WORKFLOW_1_RAW_STREAM -> {
                        geminiService.processWorkflow1RawStream(
                            transcript = effectiveTranscript,
                            detectedLang = detectedLangEnum.label,
                            audioFileName = recordingResult?.fileName,
                            audioDurationSec = (audioDurationMs / 1000L).coerceAtLeast(1L)
                        )
                    }
                    WorkflowMode.WORKFLOW_2_MIND_SYNTHESIS,
                    WorkflowMode.WORKFLOW_3_COMBO_MASTERWORK -> {
                        geminiService.processWorkflow2MindSynthesis(
                            transcript = effectiveTranscript,
                            detectedLang = detectedLangEnum.label,
                            depthMode = _cognitiveDepthMode.value,
                            formatArchetype = _selectedFormatArchetype.value,
                            customInstructions = _customPromptInstructions.value
                        )
                    }
                }

                delay(300)

                // Step 5: Secure Long-Term Storage
                _pipelineStage.value = PipelineStage.STAGE_5_ARCHIVED
                _pipelineDetail.value = "Secured in Memory Archive & Local Database!"

                val isLongTerm = (_workflowMode.value != WorkflowMode.WORKFLOW_1_RAW_STREAM)
                val initialRetentionScore = if (isLongTerm) 85 else 60

                val finalWorkflowType = when (_workflowMode.value) {
                    WorkflowMode.WORKFLOW_3_COMBO_MASTERWORK -> "COMBO_MASTERWORK"
                    WorkflowMode.WORKFLOW_2_MIND_SYNTHESIS -> "COGNITIVE_SYNTHESIS"
                    WorkflowMode.WORKFLOW_1_RAW_STREAM -> "RAW_STREAM"
                }

                val note = NoteEntity(
                    title = result.title,
                    rawTranscript = effectiveTranscript,
                    synthesizedContent = result.structuredContent,
                    workflowType = finalWorkflowType,
                    language = detectedLangEnum.label,
                    audioFilePath = audioPath,
                    audioDurationMs = audioDurationMs,
                    category = categorizeContent(effectiveTranscript),
                    tags = result.keyTerms.joinToString(", "),
                    isLongTermMemory = isLongTerm,
                    retentionScore = initialRetentionScore,
                    summary = result.summary,
                    keyTerms = result.keyTerms.joinToString("\n• "),
                    actionItems = result.actionItems.joinToString("\n")
                )

                val noteId = repository.insertNote(note)
                val savedNoteWithId = note.copy(id = noteId)

                // Save generated flashcards
                if (result.flashcards.isNotEmpty()) {
                    val entities = result.flashcards.map { prompt ->
                        FlashcardEntity(
                            noteId = noteId,
                            question = prompt.question,
                            answer = prompt.answer,
                            conceptKey = prompt.conceptKey
                        )
                    }
                    repository.insertFlashcards(entities)
                }

                _latestSavedNote.value = savedNoteWithId
                _selectedNote.value = savedNoteWithId
                _aiState.value = AiProcessingState.Success("Note created successfully in ${if (isLongTerm) "Cognitive Mind" else "Raw Audio Note"} mode!")

                delay(1200)
                _pipelineStage.value = PipelineStage.IDLE
                _pipelineDetail.value = ""
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error processing capture", e)
                _aiState.value = AiProcessingState.Error(e.localizedMessage ?: "Failed to process audio capture")
                _pipelineStage.value = PipelineStage.IDLE
            }
        }
    }

    fun refineSelectedNote(refinementType: RefinementType) {
        val current = _selectedNote.value ?: return
        viewModelScope.launch {
            _aiState.value = AiProcessingState.Processing("Applying cognitive refinement...")
            try {
                val updatedContent = geminiService.refineNoteWithAi(
                    currentContent = current.synthesizedContent,
                    refinementType = refinementType
                )
                val updatedNote = current.copy(
                    synthesizedContent = updatedContent,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateNote(updatedNote)
                _selectedNote.value = updatedNote
                _latestSavedNote.value = updatedNote
                _aiState.value = AiProcessingState.Success("Refinement applied successfully!")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Refinement failed", e)
                _aiState.value = AiProcessingState.Error("Refinement failed: ${e.localizedMessage}")
            }
        }
    }

    fun reformatSelectedNote(archetype: CognitiveFormatArchetype, customPrompt: String = "") {
        val current = _selectedNote.value ?: return
        viewModelScope.launch {
            _aiState.value = AiProcessingState.Processing("Re-synthesizing note into ${archetype.title} format...")
            try {
                val customInstructionCombined = if (customPrompt.isNotBlank()) {
                    "Format Archetype: ${archetype.title}\nArchetype Description: ${archetype.description}\nUser Requested Detail Specification:\n$customPrompt"
                } else {
                    "Format Archetype: ${archetype.title}\nArchetype Description: ${archetype.description}"
                }
                val updatedContent = geminiService.refineNoteWithAi(
                    currentContent = current.synthesizedContent,
                    refinementType = RefinementType.CUSTOM_REFORMAT,
                    customPrompt = customInstructionCombined
                )
                val updatedNote = current.copy(
                    synthesizedContent = updatedContent,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateNote(updatedNote)
                _selectedNote.value = updatedNote
                _latestSavedNote.value = updatedNote
                _aiState.value = AiProcessingState.Success("Synthesized into ${archetype.title} format!")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Reformatting failed", e)
                _aiState.value = AiProcessingState.Error("Reformatting failed: ${e.localizedMessage}")
            }
        }
    }

    fun createManualNote(title: String, content: String, category: String = "General", tags: String = "") {
        viewModelScope.launch {
            val note = NoteEntity(
                title = title.ifBlank { "Untitled Note" },
                rawTranscript = content,
                synthesizedContent = content,
                workflowType = "MANUAL_DOCUMENT",
                language = "English",
                category = category.ifBlank { "General" },
                tags = tags,
                isLongTermMemory = true,
                retentionScore = 90,
                summary = content.take(150),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val noteId = repository.insertNote(note)
            val savedNote = note.copy(id = noteId)
            _selectedNote.value = savedNote
            _latestSavedNote.value = savedNote
            _currentScreen.value = AppScreen.DETAIL
        }
    }

    fun importDocumentText(title: String, rawText: String, refineWithAi: Boolean = false) {
        importDocument(title = title, rawContent = rawText, category = "Imported", autoSynthesizeWithAi = refineWithAi)
    }

    fun importDocument(title: String, rawContent: String, category: String = "General", autoSynthesizeWithAi: Boolean = false) {
        viewModelScope.launch {
            if (autoSynthesizeWithAi) {
                processAndSaveCurrentCapture(
                    initialTranscript = rawContent,
                    audioPath = null,
                    audioDurationMs = 0L,
                    recordingResult = null
                )
            } else {
                createManualNote(title, rawContent, category)
            }
        }
    }

    fun updateNoteContent(title: String, content: String, tags: String, category: String) {
        val current = _selectedNote.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                title = title,
                synthesizedContent = content,
                tags = tags,
                category = category,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateNote(updated)
            _selectedNote.value = updated
            _latestSavedNote.value = updated
        }
    }

    fun toggleMemoryTier(note: NoteEntity) {
        viewModelScope.launch {
            val newLongTerm = !note.isLongTermMemory
            val newScore = if (newLongTerm) 90 else 65
            if (newLongTerm) {
                repository.promoteToLongTerm(note.id, newScore)
            } else {
                repository.demoteToShortTerm(note.id, newScore)
            }
            val updated = note.copy(isLongTermMemory = newLongTerm, retentionScore = newScore)
            _selectedNote.value = updated
            if (_latestSavedNote.value?.id == note.id) {
                _latestSavedNote.value = updated
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            if (_selectedNote.value?.id == note.id) {
                _selectedNote.value = null
                _currentScreen.value = AppScreen.ARCHIVE
            }
            if (_latestSavedNote.value?.id == note.id) {
                _latestSavedNote.value = null
            }
        }
    }

    fun markFlashcardReviewed(id: Long, isMastered: Boolean) {
        viewModelScope.launch {
            repository.markFlashcardReviewed(id, isMastered)
        }
    }

    fun promoteToLongTerm(noteId: Long) {
        viewModelScope.launch {
            repository.promoteToLongTerm(noteId, 95)
        }
    }

    fun consolidateAllNotes() {
        viewModelScope.launch {
            allNotes.value.forEach { note ->
                if (!note.isLongTermMemory) {
                    repository.promoteToLongTerm(note.id, 92)
                }
            }
        }
    }

    /**
     * Completely purges all stored documents, flashcards, quizzes, study plans,
     * and chat history to provide a clean slate with zero dummy data.
     */
    fun resetVaultData(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.clearEntireVault()
                _latestSavedNote.value = null
                _selectedNote.value = null
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error wiping vault database", e)
            }
        }
    }

    fun shareNote(note: NoteEntity) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.synthesizedContent}\n\n-- Transcribed & Synthesized Document")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(shareIntent, "Share Document").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }

    fun handleIncomingShare(uri: android.net.Uri?, text: String?, mimeType: String?, context: Context) {
        if (uri != null && mimeType != null) {
            if (mimeType.startsWith("audio/")) {
                importAudioFile(uri, context)
                _currentScreen.value = AppScreen.CAPTURE
            } else if (mimeType.startsWith("image/")) {
                importImageFile(uri, context)
                _currentScreen.value = AppScreen.CAPTURE
            } else {
                importDocumentText("Shared Document", text ?: "Shared attachment", refineWithAi = true)
                _currentScreen.value = AppScreen.CAPTURE
            }
        } else if (!text.isNullOrBlank()) {
            importDocumentText("Shared Spoken Note", text, refineWithAi = true)
            _currentScreen.value = AppScreen.CAPTURE
        }
    }

    private fun categorizeContent(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("meeting") || lower.contains("call") || lower.contains("client") -> "Meeting"
            lower.contains("nlp") || lower.contains("ai") || lower.contains("model") || lower.contains("machine learning") -> "NLP & AI"
            lower.contains("research") || lower.contains("study") || lower.contains("paper") -> "Research"
            lower.contains("idea") || lower.contains("brainstorm") || lower.contains("think") -> "Idea"
            lower.contains("todo") || lower.contains("task") || lower.contains("project") -> "Project"
            else -> "General"
        }
    }

    // --- SM-2 Spaced Repetition Engine ---
    fun submitSm2Review(card: FlashcardEntity, grade: SpacedRepetitionScheduler.ReviewGrade) {
        viewModelScope.launch {
            val result = SpacedRepetitionScheduler.calculateNextReview(card, grade)
            repository.updateFlashcardSm2(
                id = card.id,
                easeFactor = result.easeFactor,
                intervalDays = result.intervalDays,
                repetitions = result.repetitions,
                nextReviewAt = result.nextReviewAt,
                lastReviewedAt = System.currentTimeMillis(),
                isMastered = result.isMastered
            )
        }
    }

    // --- Ask NeuroScribe Grounded Intelligence ---
    fun askNeuroScribe(query: String, mode: AskNeuroScribeMode) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _askAiState.value = AiProcessingState.Processing("Retrieving relevant knowledge from vault...")
            val userMsg = ChatMessageEntity(
                role = "user",
                content = query,
                mode = mode.name,
                timestamp = System.currentTimeMillis()
            )
            repository.insertChatMessage(userMsg)

            try {
                val result = geminiService.askNeuroScribe(
                    query = query,
                    mode = mode,
                    candidateNotes = allNotes.value
                )
                val citationsJson = org.json.JSONArray().apply {
                    result.citedSources.forEach { citation ->
                        put(org.json.JSONObject().apply {
                            put("noteId", citation.noteId)
                            put("title", citation.title)
                            put("excerpt", citation.excerpt)
                        })
                    }
                }.toString()

                val assistantMsg = ChatMessageEntity(
                    role = "assistant",
                    content = result.answer,
                    mode = mode.name,
                    citationsJson = citationsJson,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertChatMessage(assistantMsg)
                _askAiState.value = AiProcessingState.Success("Synthesized response.")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Ask NeuroScribe error", e)
                _askAiState.value = AiProcessingState.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    // --- Quizzes & Assessments ---
    fun generateQuizForNote(note: NoteEntity) {
        viewModelScope.launch {
            _aiState.value = AiProcessingState.Processing("Generating assessment quiz for '${note.title}'...")
            try {
                val genResult = geminiService.generateQuiz(note.title, note.synthesizedContent)
                val quiz = QuizEntity(
                    noteId = note.id,
                    title = genResult.title,
                    topic = genResult.topic,
                    totalQuestions = genResult.questions.size
                )
                val questions = genResult.questions.map { q ->
                    val optsJson = org.json.JSONArray().apply {
                        q.options.forEach { put(it) }
                    }.toString()
                    QuizQuestionEntity(
                        quizId = 0,
                        questionText = q.questionText,
                        optionsJson = optsJson,
                        correctOptionIndex = q.correctIndex,
                        explanation = q.explanation
                    )
                }
                val quizId = repository.insertQuiz(quiz, questions)
                val savedQuiz = quiz.copy(id = quizId)
                _activeQuiz.value = savedQuiz
                loadQuizQuestions(quizId)
                _aiState.value = AiProcessingState.Success("Assessment quiz created!")
                _currentScreen.value = AppScreen.RETENTION
            } catch (e: Exception) {
                Log.e("MainViewModel", "Quiz generation error", e)
                _aiState.value = AiProcessingState.Error("Quiz creation failed: ${e.localizedMessage}")
            }
        }
    }

    fun loadQuizQuestions(quizId: Long) {
        viewModelScope.launch {
            val questions = repository.getQuestionsForQuizSync(quizId)
            _activeQuizQuestions.value = questions
        }
    }

    fun selectQuiz(quiz: QuizEntity) {
        _activeQuiz.value = quiz
        loadQuizQuestions(quiz.id)
    }

    fun clearActiveQuiz() {
        _activeQuiz.value = null
        _activeQuizQuestions.value = emptyList()
    }

    fun answerQuizQuestion(questionId: Long, selectedIndex: Int) {
        viewModelScope.launch {
            val currentList = _activeQuizQuestions.value
            val target = currentList.find { it.id == questionId }
            if (target != null) {
                val updated = target.copy(userSelectedOptionIndex = selectedIndex)
                repository.submitQuizAnswer(updated)
                _activeQuizQuestions.value = currentList.map { if (it.id == questionId) updated else it }
            }
        }
    }

    fun finishQuiz(quizId: Long) {
        viewModelScope.launch {
            val questions = _activeQuizQuestions.value
            val correctCount = questions.count { it.userSelectedOptionIndex == it.correctOptionIndex }
            val scorePercent = if (questions.isNotEmpty()) (correctCount * 100) / questions.size else 0
            repository.completeQuiz(quizId, scorePercent)
            val updatedQuiz = _activeQuiz.value?.copy(isCompleted = true, scoreAchieved = scorePercent)
            _activeQuiz.value = updatedQuiz
        }
    }

    // --- Study Plans ---
    fun createStudyPlan(subject: String, examDate: String, hoursPerDay: Int) {
        viewModelScope.launch {
            _aiState.value = AiProcessingState.Processing("Synthesizing personalized study roadmap...")
            try {
                val result = geminiService.generateStudyPlan(subject, examDate, hoursPerDay, allNotes.value)
                val scheduleJson = org.json.JSONArray().apply {
                    result.scheduleItems.forEach { item ->
                        put(org.json.JSONObject().apply {
                            put("phase", item.phase)
                            put("duration", item.durationText)
                            put("topics", org.json.JSONArray(item.topicsToCover))
                            put("activities", org.json.JSONArray(item.keyActivities))
                        })
                    }
                }.toString()

                val plan = StudyPlanEntity(
                    title = result.title,
                    subject = result.subject,
                    examDate = examDate,
                    hoursPerDay = hoursPerDay,
                    goals = result.goals,
                    scheduleJson = scheduleJson
                )
                repository.insertStudyPlan(plan)
                _aiState.value = AiProcessingState.Success("Personalized study plan generated!")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Study plan creation error", e)
                _aiState.value = AiProcessingState.Error("Study plan error: ${e.localizedMessage}")
            }
        }
    }

    fun toggleStudyPlan(plan: StudyPlanEntity) {
        viewModelScope.launch {
            repository.toggleStudyPlanCompleted(plan.id, !plan.isCompleted)
        }
    }

    fun deleteStudyPlan(plan: StudyPlanEntity) {
        viewModelScope.launch {
            repository.deleteStudyPlan(plan)
        }
    }

    // --- Knowledge Connections ---
    fun discoverAndSaveKnowledgeConnections() {
        viewModelScope.launch {
            val pairs = geminiService.discoverConnections(allNotes.value)
            val entities = pairs.map { p ->
                KnowledgeConnectionEntity(
                    sourceNoteId = p.sourceNoteId,
                    targetNoteId = p.targetNoteId,
                    relationshipType = p.relationship,
                    sharedConcept = p.sharedConcept
                )
            }
            repository.insertKnowledgeConnections(entities)
        }
    }

    override fun onCleared() {
        super.onCleared()
        recorderManager.cleanup()
        playerManager.cleanup()
        speechManager.cleanup()
    }
}
