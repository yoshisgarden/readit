package com.yoshisgarden.readit.data

import androidx.room.Dao
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
        "SELECT * FROM phrases WHERE english LIKE '%' || :q || '%' " +
            "OR japanese LIKE '%' || :q || '%' " +
            "OR exampleEn LIKE '%' || :q || '%' ORDER BY id",
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

    @Query("SELECT * FROM phrases WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<Phrase>
}

data class CategoryCount(val category: String, val cnt: Int)

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

    @Query("SELECT COUNT(*) FROM flashcards WHERE reviewCount > 0")
    fun observeStudiedCount(): Flow<Int>
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
