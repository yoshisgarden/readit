package com.yoshisgarden.readit.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity definitions for ReadIT.
 *
 * Tables mirror the design document (section 5.1):
 *   phrases / flashcards / quiz_results / study_logs / user_progress
 */

@Entity(tableName = "phrases")
data class Phrase(
    @PrimaryKey val id: Long,
    val english: String,
    val japanese: String,
    val category: String,
    val exampleEn: String,
    val exampleJa: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = 0L,
)

@Entity(
    tableName = "flashcards",
    indices = [Index(value = ["phraseId"], unique = true)],
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phraseId: Long,
    /** Current SRS interval in days. */
    val interval: Int = 0,
    /** SM-2 ease factor (>= 1.3). */
    val easeFactor: Double = 2.5,
    /** Next review time as epoch millis. */
    val dueDate: Long = 0L,
    /** Last rating: 0 = unknown, 1 = vague, 2 = known. */
    val rating: Int = 0,
    val reviewCount: Int = 0,
)

/**
 * Append-only record of every flashcard answer.
 *
 * [Flashcard] only keeps the *latest* rating (SM-2 overwrites it each time), so it
 * cannot answer "which phrases did I keep missing?" or "what was I weak on last
 * time?". This table keeps the full history for the review phase and the weak list.
 */
@Entity(
    tableName = "review_logs",
    indices = [Index("phraseId"), Index("sessionId")],
)
data class ReviewLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phraseId: Long,
    /** 0 = 知らない, 1 = うっすら, 2 = 知ってる (matches Sm2Rating.storedValue). */
    val rating: Int,
    val answeredAt: Long,
    /** Groups answers into one study session (session start, epoch millis). */
    val sessionId: Long,
    /** True when this is a same-session re-show of a card the user just missed. */
    val isRetry: Boolean = false,
)

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** "fill_blank" / "error_analysis" / "doc_reading" */
    val quizMode: String,
    val score: Int,
    val total: Int,
    /** Phrase ids the user answered incorrectly. */
    val weakPhraseIds: List<Long> = emptyList(),
    val createdAt: Long = 0L,
)

@Entity(tableName = "study_logs")
data class StudyLog(
    @PrimaryKey val date: String, // "yyyy-MM-dd" (one row per day)
    val durationMin: Int = 0,
    val phrasesStudied: Int = 0,
    val phase: Int = 1,
)

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1, // singleton row
    val currentPhase: Int = 1,
    val streakDays: Int = 0,
    val totalPhrases: Int = 0,
    val lastStudyDate: String = "", // "yyyy-MM-dd"
    val phaseStartDate: Long = 0L,
)
