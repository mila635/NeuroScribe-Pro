package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val mode: String = "MY_KNOWLEDGE", // "MY_KNOWLEDGE" or "ADVANCED_INTELLIGENCE"
    val citationsJson: String = "[]", // JSON array of cited document references: [{"docId": 12, "title": "...", "snippet": "..."}]
    val timestamp: Long = System.currentTimeMillis()
)
