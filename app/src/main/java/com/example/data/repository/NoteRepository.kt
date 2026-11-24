package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.KnowledgeConnectionEntity
import com.example.data.model.NoteEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.StudyPlanEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val database: AppDatabase) {
    private val noteDao = database.noteDao()
    private val flashcardDao = database.flashcardDao()
    private val knowledgeDao = database.knowledgeDao()
    private val quizDao = database.quizDao()
    private val studyPlanDao = database.studyPlanDao()
    private val chatDao = database.chatDao()

    // Notes
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getLongTermNotes(): Flow<List<NoteEntity>> = noteDao.getLongTermNotes()

    fun getShortTermNotes(): Flow<List<NoteEntity>> = noteDao.getShortTermNotes()

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun promoteToLongTerm(id: Long, retentionScore: Int = 95) {
        noteDao.updateRetentionStatus(id, isLongTerm = true, retentionScore = retentionScore)
    }

    suspend fun demoteToShortTerm(id: Long, retentionScore: Int = 70) {
        noteDao.updateRetentionStatus(id, isLongTerm = false, retentionScore = retentionScore)
    }

    // Flashcards & Spaced Repetition (SM-2)
    fun getAllFlashcards(): Flow<List<FlashcardEntity>> = flashcardDao.getAllFlashcards()

    fun getFlashcardsForNote(noteId: Long): Flow<List<FlashcardEntity>> = flashcardDao.getFlashcardsForNote(noteId)

    fun getDueFlashcards(currentTime: Long = System.currentTimeMillis()): Flow<List<FlashcardEntity>> =
        flashcardDao.getDueFlashcards(currentTime)

    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>) = flashcardDao.insertFlashcards(flashcards)

    suspend fun markFlashcardReviewed(id: Long, isMastered: Boolean) =
        flashcardDao.markReviewed(id, isMastered)

    suspend fun updateFlashcardSm2(
        id: Long,
        easeFactor: Float,
        intervalDays: Int,
        repetitions: Int,
        nextReviewAt: Long,
        lastReviewedAt: Long,
        isMastered: Boolean
    ) = flashcardDao.updateSm2Schedule(
        id = id,
        easeFactor = easeFactor,
        intervalDays = intervalDays,
        repetitions = repetitions,
        nextReviewAt = nextReviewAt,
        lastReviewedAt = lastReviewedAt,
        isMastered = isMastered
    )

    // Knowledge Connections
    fun getAllKnowledgeConnections(): Flow<List<KnowledgeConnectionEntity>> =
        knowledgeDao.getAllConnections()

    fun getConnectionsForNote(noteId: Long): Flow<List<KnowledgeConnectionEntity>> =
        knowledgeDao.getConnectionsForNote(noteId)

    suspend fun insertKnowledgeConnection(connection: KnowledgeConnectionEntity): Long =
        knowledgeDao.insertConnection(connection)

    suspend fun insertKnowledgeConnections(connections: List<KnowledgeConnectionEntity>) =
        knowledgeDao.insertConnections(connections)

    // Quizzes & Assessments
    fun getAllQuizzes(): Flow<List<QuizEntity>> = quizDao.getAllQuizzes()

    suspend fun getQuizById(id: Long): QuizEntity? = quizDao.getQuizById(id)

    fun getQuestionsForQuiz(quizId: Long): Flow<List<QuizQuestionEntity>> =
        quizDao.getQuestionsForQuiz(quizId)

    suspend fun getQuestionsForQuizSync(quizId: Long): List<QuizQuestionEntity> =
        quizDao.getQuestionsForQuizSync(quizId)

    suspend fun insertQuiz(quiz: QuizEntity, questions: List<QuizQuestionEntity>): Long {
        val quizId = quizDao.insertQuiz(quiz)
        val linkedQuestions = questions.map { it.copy(quizId = quizId) }
        quizDao.insertQuestions(linkedQuestions)
        return quizId
    }

    suspend fun submitQuizAnswer(question: QuizQuestionEntity) =
        quizDao.updateQuestion(question)

    suspend fun completeQuiz(quizId: Long, score: Int) =
        quizDao.completeQuiz(quizId, score)

    suspend fun deleteQuiz(quiz: QuizEntity) =
        quizDao.deleteQuiz(quiz)

    // Study Plans
    fun getAllStudyPlans(): Flow<List<StudyPlanEntity>> = studyPlanDao.getAllStudyPlans()

    suspend fun getStudyPlanById(id: Long): StudyPlanEntity? = studyPlanDao.getStudyPlanById(id)

    suspend fun insertStudyPlan(plan: StudyPlanEntity): Long = studyPlanDao.insertStudyPlan(plan)

    suspend fun updateStudyPlan(plan: StudyPlanEntity) = studyPlanDao.updateStudyPlan(plan)

    suspend fun toggleStudyPlanCompleted(id: Long, isCompleted: Boolean) =
        studyPlanDao.toggleCompleted(id, isCompleted)

    suspend fun deleteStudyPlan(plan: StudyPlanEntity) = studyPlanDao.deleteStudyPlan(plan)

    // Ask NeuroScribe Chat History
    fun getChatMessages(): Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun insertChatMessage(message: ChatMessageEntity): Long =
        chatDao.insertMessage(message)

    suspend fun clearChatHistory() = chatDao.clearHistory()

    suspend fun clearEntireVault() {
        database.clearAllTables()
    }
}
