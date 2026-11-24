package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.KnowledgeConnectionEntity
import com.example.data.model.NoteEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.StudyPlanEntity

@Database(
    entities = [
        NoteEntity::class,
        FlashcardEntity::class,
        KnowledgeConnectionEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        StudyPlanEntity::class,
        ChatMessageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun quizDao(): QuizDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 1 to 2 migration (notes columns, imageFilePath)
                try {
                    db.execSQL("ALTER TABLE notes ADD COLUMN imageFilePath TEXT")
                } catch (e: Exception) {
                    // Column might already exist
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add SM-2 columns to flashcards if they don't exist
                try {
                    db.execSQL("ALTER TABLE flashcards ADD COLUMN easeFactor REAL NOT NULL DEFAULT 2.5")
                } catch (e: Exception) {}
                try {
                    db.execSQL("ALTER TABLE flashcards ADD COLUMN intervalDays INTEGER NOT NULL DEFAULT 1")
                } catch (e: Exception) {}
                try {
                    db.execSQL("ALTER TABLE flashcards ADD COLUMN repetitions INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {}
                try {
                    db.execSQL("ALTER TABLE flashcards ADD COLUMN nextReviewAt INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {}

                // Create knowledge_connections
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS knowledge_connections (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sourceNoteId INTEGER NOT NULL,
                        targetNoteId INTEGER NOT NULL,
                        relationshipType TEXT NOT NULL,
                        sharedConcept TEXT NOT NULL,
                        strengthScore REAL NOT NULL DEFAULT 1.0,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(sourceNoteId) REFERENCES notes(id) ON DELETE CASCADE,
                        FOREIGN KEY(targetNoteId) REFERENCES notes(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_knowledge_connections_sourceNoteId ON knowledge_connections(sourceNoteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_knowledge_connections_targetNoteId ON knowledge_connections(targetNoteId)")

                // Create quizzes
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS quizzes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        noteId INTEGER,
                        title TEXT NOT NULL,
                        topic TEXT NOT NULL,
                        totalQuestions INTEGER NOT NULL DEFAULT 0,
                        scoreAchieved INTEGER,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(noteId) REFERENCES notes(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_quizzes_noteId ON quizzes(noteId)")

                // Create quiz_questions
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS quiz_questions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        quizId INTEGER NOT NULL,
                        questionText TEXT NOT NULL,
                        questionType TEXT NOT NULL DEFAULT 'MULTIPLE_CHOICE',
                        optionsJson TEXT NOT NULL,
                        correctOptionIndex INTEGER NOT NULL,
                        explanation TEXT NOT NULL DEFAULT '',
                        userSelectedOptionIndex INTEGER,
                        FOREIGN KEY(quizId) REFERENCES quizzes(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_questions_quizId ON quiz_questions(quizId)")

                // Create study_plans
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS study_plans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        subject TEXT NOT NULL,
                        examDate TEXT NOT NULL,
                        hoursPerDay INTEGER NOT NULL,
                        goals TEXT NOT NULL,
                        scheduleJson TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create chat_messages
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        role TEXT NOT NULL,
                        content TEXT NOT NULL,
                        mode TEXT NOT NULL DEFAULT 'MY_KNOWLEDGE',
                        citationsJson TEXT NOT NULL DEFAULT '[]',
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neuroscribe_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
