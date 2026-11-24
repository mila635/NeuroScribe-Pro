package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subject: String,
    val examDate: String,
    val hoursPerDay: Int,
    val goals: String,
    val scheduleJson: String, // JSON structured roadmap with milestone days and topics
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
