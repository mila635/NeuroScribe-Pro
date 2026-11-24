package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StudyPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans ORDER BY createdAt DESC")
    fun getAllStudyPlans(): Flow<List<StudyPlanEntity>>

    @Query("SELECT * FROM study_plans WHERE id = :id LIMIT 1")
    suspend fun getStudyPlanById(id: Long): StudyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlan(plan: StudyPlanEntity): Long

    @Update
    suspend fun updateStudyPlan(plan: StudyPlanEntity)

    @Delete
    suspend fun deleteStudyPlan(plan: StudyPlanEntity)

    @Query("UPDATE study_plans SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun toggleCompleted(id: Long, isCompleted: Boolean)
}
