package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isLongTermMemory = 1 ORDER BY createdAt DESC")
    fun getLongTermNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isLongTermMemory = 0 ORDER BY createdAt DESC")
    fun getShortTermNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR rawTranscript LIKE '%' || :query || '%' OR synthesizedContent LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("UPDATE notes SET isLongTermMemory = :isLongTerm, retentionScore = :retentionScore, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRetentionStatus(id: Long, isLongTerm: Boolean, retentionScore: Int, updatedAt: Long = System.currentTimeMillis())
}
