package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
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
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val question: String,
    val answer: String,
    val conceptKey: String = "",
    val recalledCount: Int = 0,
    val lastReviewedAt: Long = 0L,
    val isMastered: Boolean = false,
    // SM-2 Spaced Repetition Algorithm fields
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val nextReviewAt: Long = 0L
)
