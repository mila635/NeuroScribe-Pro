package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.KnowledgeConnectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_connections ORDER BY createdAt DESC")
    fun getAllConnections(): Flow<List<KnowledgeConnectionEntity>>

    @Query("SELECT * FROM knowledge_connections WHERE sourceNoteId = :noteId OR targetNoteId = :noteId")
    fun getConnectionsForNote(noteId: Long): Flow<List<KnowledgeConnectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: KnowledgeConnectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnections(connections: List<KnowledgeConnectionEntity>)

    @Delete
    suspend fun deleteConnection(connection: KnowledgeConnectionEntity)

    @Query("DELETE FROM knowledge_connections WHERE sourceNoteId = :noteId OR targetNoteId = :noteId")
    suspend fun deleteConnectionsForNote(noteId: Long)
}
