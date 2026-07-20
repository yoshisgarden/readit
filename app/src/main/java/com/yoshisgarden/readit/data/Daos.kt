package com.yoshisgarden.readit.data

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(phrases: List<Phrase>)

    @Query("SELECT COUNT(*) FROM phrases")
    suspend fun count(): Int

    @Query("SELECT english FROM phrases WHERE isFavorite = 1")
    suspend fun favoriteEnglishes(): List<String>

    @Query("UPDATE phrases SET isFavorite = 1 WHERE english IN (:englishes)")
    suspend fun markFavoritesByEnglish(englishes: List<String>)

    @Query("DELETE FROM phrases")
    suspend fun clearAll()

    @Query("SELECT * FROM phrases ORDER BY id")
    fun observeAll(): Flow<List<Phrase>>

    @Query("SELECT * FROM phrases WHERE category = :category ORDER BY id")
    fun observeByCategory(category: String): Flow<List<Phrase>>

    @Query(
        "SELECT * FROM phrases " +
            "WHERE english LIKE '%' || :q || '%' " +
            "OR japanese LIKE '%' || :q || '%' " +
            "OR exampleEn LIKE '%' || :q || '%' " +
            "ORDER BY CASE " +
            "WHEN english LIKE :q || '%' THEN 0 " +
            "WHEN english LIKE '%' || :q || '%' THEN 1 " +
            "WHEN japanese LIKE :q || '%' THEN 2 " +
            "ELSE 3 END, id",
    )
    fun search(q: String): Flow<List<Phrase>>

    @Query("SELECT * FROM phrases WHERE isFavorite = 1 ORDER BY id")
    fun observeFavorites(): Flow<List<Phrase>>

    @Query("SELECT * FROM phrases WHERE id = :id")
    fun observeById(id: Long): Flow<Phrase?>

    @Query("SELECT * FROM phrases WHERE id = :id")
    suspend fun getById(id: Long): Phrase?

    @Query("UPDATE phrases SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("SELECT category, COUNT(*) AS cnt FROM phrases GROUP BY category")
    fun observeCategoryCounts(): Flow<List<CategoryCount>>

    @Query("SELECT * FROM phrases ORDER BY id LIMIT :limit OFFSET :offset")
    suspend fun page(limit: Int, offset: Int): List<Phrase>

    @Query("SELECT * FROM phrases ORDER BY RANDOM() LIMIT :n")
    suspend fun randomPhrases(n: Int): List<Phrase>

    @Query("SELECT * FROM phrases WHERE category IN (:categories) ORDER BY RANDOM() LIMIT :n")
    suspend fun randomByCategories(categories: List<String>, n: Int): List<Phrase>

    @Query("SELECT * FROM phrases WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<Phrase>

    /**
     * Due cards, weakest first (rating 0 = 知らない, 1 = legacy うっすら, 2 = 知ってる),
     * then soonest-due. Joining here keeps the ordering, which `getByIds` loses.
     */
    @Query(
        "SELECT p.* FROM phrases p " +
            "INNER JOIN flashcards f ON f.phraseId = p.id " +
            "WHERE f.dueDate <= :now " +
            "ORDER BY f.rating, f.dueDate LIMIT :limit",
    )
    suspend fun dueWeakestFirst(now: Long, limit: Int): List<Phrase>

    /** Phrases that have never been rated — the real "new cards" pool. */
    @Query(
        "SELECT * FROM phrases WHERE id NOT IN (SELECT phraseId FROM flashcards) " +
            "ORDER BY RANDOM() LIMIT :n",
    )
    suspend fun randomUnstudied(n: Int): List<Phrase>

    /** Not due yet — last-resort filler once everything has been studied. */
    @Query(
        "SELECT p.* FROM phrases p " +
            "INNER JOIN flashcards f ON f.phraseId = p.id " +
            "WHERE f.dueDate > :now " +
            "ORDER BY f.rating, f.dueDate LIMIT :n",
    )
    suspend fun upcomingSoonest(now: Long, n: Int): List<Phrase>
}

data class CategoryCount(val category: String, val cnt: Int)

/** A phrase the user has missed at least once, with its answer tallies. */
data class WeakPhrase(
    @Embedded val phrase: Phrase,
    val unknownCount: Int,
    val vagueCount: Int,
    val knownCount: Int,
    val lastReviewed: Long,
)

@Dao
interface FlashcardDao {
    @Upsert
    suspend fun upsert(card: Flashcard)

    @Query("SELECT * FROM flashcards WHERE phraseId = :phraseId LIMIT 1")
    suspend fun getByPhrase(phraseId: Long): Flashcard?

    @Query("SELECT * FROM flashcards WHERE dueDate <= :now ORDER BY dueDate LIMIT :limit")
    suspend fun due(now: Long, limit: Int): List<Flashcard>

    @Query("SELECT COUNT(*) FROM flashcards WHERE dueDate <= :now")
    fun observeDueCount(now: Long): Flow<Int>

    /** Cards available to study now: due flashcards + phrases never studied yet. */
    @Query(
        "SELECT (SELECT COUNT(*) FROM flashcards WHERE dueDate <= :now) " +
            "+ (SELECT COUNT(*) FROM phrases WHERE id NOT IN (SELECT phraseId FROM flashcards))",
    )
    fun observeStudyableCount(now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE reviewCount > 0")
    fun observeStudiedCount(): Flow<Int>
}

@Dao
interface ReviewLogDao {
    @Insert
    suspend fun insert(log: ReviewLog)

    /**
     * Id of the most recent session. Call this *before* writing any answer of the
     * new session, otherwise it returns the session in progress.
     */
    @Query("SELECT MAX(sessionId) FROM review_logs")
    suspend fun latestSessionId(): Long?

    @Query("SELECT COUNT(*) FROM review_logs WHERE sessionId = :sessionId AND phraseId = :phraseId")
    suspend fun countAnswers(sessionId: Long, phraseId: Long): Int

    /**
     * Phrases whose *final* answer in [sessionId] was 知らない (or legacy うっすら),
     * weakest first.
     * The `MAX(id)` sub-select picks the last answer so a card retried until 知ってる
     * is not dragged into the next session's review phase.
     */
    @Query(
        "SELECT r.phraseId FROM review_logs r " +
            "WHERE r.sessionId = :sessionId AND r.rating < 2 " +
            "AND r.id = (SELECT MAX(id) FROM review_logs " +
            "WHERE sessionId = :sessionId AND phraseId = r.phraseId) " +
            "ORDER BY r.rating, r.id LIMIT :limit",
    )
    suspend fun weakPhraseIdsInSession(sessionId: Long, limit: Int): List<Long>

    /** Every phrase missed at least once, most-missed first. */
    @Query(
        "SELECT p.*, " +
            "SUM(CASE WHEN r.rating = 0 THEN 1 ELSE 0 END) AS unknownCount, " +
            "SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END) AS vagueCount, " +
            "SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END) AS knownCount, " +
            "MAX(r.answeredAt) AS lastReviewed " +
            "FROM review_logs r INNER JOIN phrases p ON p.id = r.phraseId " +
            "GROUP BY r.phraseId " +
            "HAVING unknownCount + vagueCount > 0 " +
            "ORDER BY unknownCount DESC, vagueCount DESC, lastReviewed DESC",
    )
    fun observeWeakPhrases(): Flow<List<WeakPhrase>>
}

@Dao
interface QuizResultDao {
    @Insert
    suspend fun insert(result: QuizResult): Long

    @Query("SELECT * FROM quiz_results ORDER BY createdAt DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<QuizResult>>

    @Query("SELECT * FROM quiz_results ORDER BY createdAt DESC")
    suspend fun all(): List<QuizResult>
}

@Dao
interface StudyLogDao {
    @Upsert
    suspend fun upsert(log: StudyLog)

    @Query("SELECT * FROM study_logs WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): StudyLog?

    @Query("SELECT * FROM study_logs ORDER BY date DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<StudyLog>>

    @Query("SELECT * FROM study_logs ORDER BY date")
    suspend fun all(): List<StudyLog>
}

@Dao
interface UserProgressDao {
    @Upsert
    suspend fun upsert(progress: UserProgress)

    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun observe(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun get(): UserProgress?
}
