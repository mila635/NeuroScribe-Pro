package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quizzes",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["noteId"])]
)
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long?,
    val title: String,
    val topic: String,
    val totalQuestions: Int = 0,
    val scoreAchieved: Int? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quizId"])]
)
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizId: Long,
    val questionText: String,
    val questionType: String = "MULTIPLE_CHOICE", // "MULTIPLE_CHOICE", "TRUE_FALSE"
    val optionsJson: String, // JSON array of options e.g. ["A", "B", "C", "D"]
    val correctOptionIndex: Int,
    val explanation: String = "",
    val userSelectedOptionIndex: Int? = null
)
