package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY lastReviewedAt ASC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE noteId = :noteId")
    fun getFlashcardsForNote(noteId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE nextReviewAt <= :currentTime OR nextReviewAt = 0 ORDER BY nextReviewAt ASC")
    fun getDueFlashcards(currentTime: Long = System.currentTimeMillis()): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)

    @Query("UPDATE flashcards SET recalledCount = recalledCount + 1, isMastered = :isMastered, lastReviewedAt = :timestamp WHERE id = :id")
    suspend fun markReviewed(id: Long, isMastered: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE flashcards 
        SET easeFactor = :easeFactor, 
            intervalDays = :intervalDays, 
            repetitions = :repetitions, 
            nextReviewAt = :nextReviewAt, 
            lastReviewedAt = :lastReviewedAt, 
            recalledCount = recalledCount + 1, 
            isMastered = :isMastered 
        WHERE id = :id
    """)
    suspend fun updateSm2Schedule(
        id: Long,
        easeFactor: Float,
        intervalDays: Int,
        repetitions: Int,
        nextReviewAt: Long,
        lastReviewedAt: Long,
        isMastered: Boolean
    )
}
