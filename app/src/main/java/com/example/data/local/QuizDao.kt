package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id LIMIT 1")
    suspend fun getQuizById(id: Long): QuizEntity?

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId")
    fun getQuestionsForQuiz(quizId: Long): Flow<List<QuizQuestionEntity>>

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId")
    suspend fun getQuestionsForQuizSync(quizId: Long): List<QuizQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Update
    suspend fun updateQuestion(question: QuizQuestionEntity)

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)

    @Query("UPDATE quizzes SET scoreAchieved = :score, isCompleted = 1 WHERE id = :quizId")
    suspend fun completeQuiz(quizId: Long, score: Int)
}
