package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "knowledge_connections",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceNoteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetNoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sourceNoteId"]),
        Index(value = ["targetNoteId"])
    ]
)
data class KnowledgeConnectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceNoteId: Long,
    val targetNoteId: Long,
    val relationshipType: String, // e.g., "PREREQUISITE", "ANALOGY", "DEEPENS", "CONTRASTS", "CROSS_REFERENCE"
    val sharedConcept: String,
    val strengthScore: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis()
)
