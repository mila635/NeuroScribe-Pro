package com.example.util

import com.example.data.model.FlashcardEntity
import kotlin.math.roundToInt

/**
 * SuperMemo SM-2 Spaced Repetition Algorithm Implementation.
 * Calculates optimal retention review intervals based on user feedback score (0-5).
 */
object SpacedRepetitionScheduler {

    enum class ReviewGrade(val quality: Int, val label: String) {
        AGAIN(quality = 1, label = "Again"),
        HARD(quality = 3, label = "Hard"),
        GOOD(quality = 4, label = "Good"),
        EASY(quality = 5, label = "Easy")
    }

    data class Sm2Result(
        val easeFactor: Float,
        val intervalDays: Int,
        val repetitions: Int,
        val nextReviewAt: Long,
        val isMastered: Boolean
    )

    fun calculateNextReview(
        card: FlashcardEntity,
        grade: ReviewGrade,
        currentTime: Long = System.currentTimeMillis()
    ): Sm2Result {
        return calculateNextReview(
            easeFactor = card.easeFactor,
            intervalDays = card.intervalDays,
            repetitions = card.repetitions,
            quality = grade.quality,
            currentTime = currentTime
        )
    }

    fun calculateNextReview(
        easeFactor: Float,
        intervalDays: Int,
        repetitions: Int,
        quality: Int,
        currentTime: Long = System.currentTimeMillis()
    ): Sm2Result {
        val q = quality.coerceIn(0, 5)

        val newRepetitions: Int
        val newIntervalDays: Int

        if (q >= 3) {
            newRepetitions = repetitions + 1
            newIntervalDays = when (repetitions) {
                0 -> 1
                1 -> 6
                else -> (intervalDays * easeFactor).roundToInt().coerceAtLeast(1)
            }
        } else {
            newRepetitions = 0
            newIntervalDays = 1
        }

        // SM-2 Ease Factor calculation
        val efDelta = 0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f)
        val newEaseFactor = (easeFactor + efDelta).coerceAtLeast(1.3f)

        val dayInMillis = 86_400_000L
        val nextReviewAt = currentTime + (newIntervalDays * dayInMillis)

        val isMastered = newRepetitions >= 3 && newEaseFactor >= 2.0f

        return Sm2Result(
            easeFactor = newEaseFactor,
            intervalDays = newIntervalDays,
            repetitions = newRepetitions,
            nextReviewAt = nextReviewAt,
            isMastered = isMastered
        )
    }
}
