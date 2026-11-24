package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val rawTranscript: String,
    val synthesizedContent: String,
    val workflowType: String, // "RAW_STREAM" or "COGNITIVE_SYNTHESIS"
    val language: String, // "English", "Roman Urdu", "Urdu", "Multilingual"
    val audioFilePath: String? = null,
    val audioDurationMs: Long = 0L,
    val imageFilePath: String? = null,
    val category: String = "General",
    val tags: String = "",
    val isLongTermMemory: Boolean = false,
    val retentionScore: Int = 80,
    val summary: String = "",
    val keyTerms: String = "", // Comma-separated or bullet list of extracted NLP terms
    val actionItems: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
