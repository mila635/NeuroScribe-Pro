package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiCognitiveService
import com.example.ai.RefinementType
import com.example.audio.AudioRecorderManager
import com.example.audio.AuditoryFilterState
import com.example.audio.DetectedLanguage
import com.example.audio.VoiceActivityFilter
import com.example.data.local.AppDatabase
import com.example.data.model.NoteEntity
import com.example.data.repository.NoteRepository
import com.example.ui.components.cleanAsterisks
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches NeuroScribe Pro`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NeuroScribe Pro", appName)
    }

    @Test
    fun `clean asterisks strips markdown bold and italic markers`() {
        val raw = "**Executive Summary:** This is a *crucial* point."
        val cleaned = cleanAsterisks(raw)
        assertEquals("Executive Summary: This is a crucial point.", cleaned)
        assertFalse(cleaned.contains("*"))
    }

    @Test
    fun `audio recorder stops and produces persistent file result`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val recorder = AudioRecorderManager(context)

        val path = recorder.startRecording()
        assertNotNull(path)

        val result = recorder.stopRecording()
        assertNotNull(result)
        assertTrue(result?.filePath?.endsWith(".m4a") == true)
        val file = File(result!!.filePath)
        assertTrue(file.exists())
    }

    @Test
    fun `voice activity filter detects language correctly`() {
        // Urdu script
        val urduText = "آج کا اجلاس بہت اہم ہے"
        val urduDetected = VoiceActivityFilter.detectLanguage(urduText)
        assertEquals(DetectedLanguage.URDU, urduDetected)

        // Roman Urdu
        val romanText = "yeh kaam hum kal karenge aur bohot acha hoga"
        val romanDetected = VoiceActivityFilter.detectLanguage(romanText)
        assertEquals(DetectedLanguage.ROMAN_URDU, romanDetected)

        // English
        val englishText = "We are discussing natural language processing and echoic memory buffers"
        val englishDetected = VoiceActivityFilter.detectLanguage(englishText)
        assertEquals(DetectedLanguage.ENGLISH, englishDetected)
    }

    @Test
    fun `voice activity filter distinguishes noise and speech`() {
        // High amplitude or words = speech
        val stateSpeech = VoiceActivityFilter.evaluateAudioState(0.2f, hasTranscribedWords = true)
        assertEquals(AuditoryFilterState.SPEECH_NLP_DETECTED, stateSpeech)

        // Low amplitude = filtering noise
        val stateNoise = VoiceActivityFilter.evaluateAudioState(0.03f, hasTranscribedWords = false)
        assertEquals(AuditoryFilterState.FILTERING_NOISE, stateNoise)

        // Near zero = idle
        val stateIdle = VoiceActivityFilter.evaluateAudioState(0.005f, hasTranscribedWords = false)
        assertEquals(AuditoryFilterState.IDLE, stateIdle)
    }

    @Test
    fun `room database inserts and retrieves notes`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repo = NoteRepository(db)

        val note = NoteEntity(
            title = "Test Cognitive Note",
            rawTranscript = "Testing auditory cognitive memory intake",
            synthesizedContent = "# Test Cognitive Note\nSynthesized content here.",
            workflowType = "COGNITIVE_SYNTHESIS",
            language = "English",
            isLongTermMemory = true
        )

        val id = repo.insertNote(note)
        assertTrue(id > 0)

        val retrieved = repo.getNoteById(id)
        assertNotNull(retrieved)
        assertEquals("Test Cognitive Note", retrieved?.title)
        assertTrue(retrieved?.isLongTermMemory == true)
    }

    @Test
    fun `cognitive service generates structured synthesis offline`() = runBlocking {
        val service = GeminiCognitiveService()
        val result = service.processWorkflow2MindSynthesis(
            transcript = "Aaj humne brain short term memory aur long term retention pe deep research ki.",
            detectedLang = "Roman Urdu"
        )

        assertNotNull(result.title)
        assertTrue(result.structuredContent.isNotEmpty())
        assertTrue(result.structuredContent.contains("#"))
        assertTrue(result.keyTerms.isNotEmpty())
        assertTrue(result.flashcards.isNotEmpty())
    }

    @Test
    fun `viewmodel toggles dark theme correctly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = MainViewModel(app)

        val initial = vm.isDarkTheme.value
        vm.toggleDarkTheme()
        assertEquals(!initial, vm.isDarkTheme.value)
        vm.toggleDarkTheme()
        assertEquals(initial, vm.isDarkTheme.value)
    }

    @Test
    fun `spaced repetition scheduler computes SM-2 accurately`() {
        val card = com.example.data.model.FlashcardEntity(
            id = 1,
            noteId = 1,
            question = "What is working memory capacity?",
            answer = "7 plus or minus 2 items according to George Miller",
            easeFactor = 2.5f,
            intervalDays = 1,
            repetitions = 0
        )

        // Grade 1: Again (lapse)
        val againRes = com.example.util.SpacedRepetitionScheduler.calculateNextReview(card, com.example.util.SpacedRepetitionScheduler.ReviewGrade.AGAIN)
        assertEquals(1, againRes.intervalDays)
        assertEquals(0, againRes.repetitions)
        assertFalse(againRes.isMastered)

        // Grade 4: Good (first recall)
        val goodRes1 = com.example.util.SpacedRepetitionScheduler.calculateNextReview(card, com.example.util.SpacedRepetitionScheduler.ReviewGrade.GOOD)
        assertEquals(1, goodRes1.intervalDays)
        assertEquals(1, goodRes1.repetitions)

        // Grade 4: Good (second recall)
        val cardRep1 = card.copy(repetitions = 1, intervalDays = 1)
        val goodRes2 = com.example.util.SpacedRepetitionScheduler.calculateNextReview(cardRep1, com.example.util.SpacedRepetitionScheduler.ReviewGrade.GOOD)
        assertEquals(6, goodRes2.intervalDays)
        assertEquals(2, goodRes2.repetitions)

        // Ease factor lower bound safety (>= 1.3)
        val lowEfCard = card.copy(easeFactor = 1.35f)
        val degradedRes = com.example.util.SpacedRepetitionScheduler.calculateNextReview(lowEfCard, com.example.util.SpacedRepetitionScheduler.ReviewGrade.AGAIN)
        assertTrue("Ease factor must never drop below 1.3", degradedRes.easeFactor >= 1.3f)
    }

    @Test
    fun `grounded Ask NeuroScribe retrieves citations and answers correctly`() = runBlocking {
        val service = GeminiCognitiveService()
        val testNote = NoteEntity(
            id = 42,
            title = "Neural Plasticity Research",
            rawTranscript = "Synaptic connections strengthen with long term potentiation.",
            synthesizedContent = "Neuroplasticity allows biological neurons to adjust synaptic weights and adapt structurally.",
            workflowType = "COGNITIVE_SYNTHESIS",
            language = "English"
        )

        val response = service.askNeuroScribe(
            query = "What is neuroplasticity?",
            mode = com.example.ai.AskNeuroScribeMode.MY_KNOWLEDGE,
            candidateNotes = listOf(testNote)
        )

        assertNotNull(response.answer)
        assertTrue(response.answer.isNotEmpty())
        assertTrue("Mode A must provide citations", response.citedSources.isNotEmpty())
        assertEquals(42L, response.citedSources.first().noteId)
    }

    @Test
    fun `quizzes and study plans database persistence works`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repo = NoteRepository(db)

        // Quiz insertion & question retrieval
        val quiz = com.example.data.model.QuizEntity(
            noteId = 1,
            title = "Cognitive Architecture Assessment",
            topic = "Working Memory",
            totalQuestions = 1
        )
        val q1 = com.example.data.model.QuizQuestionEntity(
            quizId = 0,
            questionText = "Which brain structure is central to episodic memory consolidation?",
            optionsJson = "[\"Hippocampus\",\"Cerebellum\",\"Occipital Lobe\",\"Pons\"]",
            correctOptionIndex = 0,
            explanation = "The hippocampus plays a critical role in episodic and declarative memory formation."
        )
        val quizId = repo.insertQuiz(quiz, listOf(q1))
        assertTrue(quizId > 0)

        val questions = repo.getQuestionsForQuizSync(quizId)
        assertEquals(1, questions.size)
        assertEquals(0, questions[0].correctOptionIndex)

        // Submit answer & finish quiz
        repo.submitQuizAnswer(questions[0].copy(userSelectedOptionIndex = 0))
        repo.completeQuiz(quizId, 100)

        // Study plan insertion
        val plan = com.example.data.model.StudyPlanEntity(
            title = "Neuroscience Exam Prep",
            subject = "Cognitive Psychology",
            examDate = "2026-12-15",
            hoursPerDay = 3,
            goals = "Master all 12 neural pathways",
            scheduleJson = "[]"
        )
        val planId = repo.insertStudyPlan(plan)
        assertTrue(planId > 0)

        repo.toggleStudyPlanCompleted(planId, true)
    }
}
