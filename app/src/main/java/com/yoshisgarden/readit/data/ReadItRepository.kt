package com.yoshisgarden.readit.data

import android.content.Context
import com.yoshisgarden.readit.srs.Sm2
import com.yoshisgarden.readit.srs.Sm2Rating
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Phrase categories used across the app (design doc 5.2). */
object Categories {
    const val CLAUDE_CODE = "Claude Code"
    const val GITHUB = "GitHub"
    const val API_DOCS = "API Docs"
    const val ERROR_MESSAGES = "Error Messages"
    const val PR_REVIEW = "PR Review"

    val ALL = listOf(CLAUDE_CODE, GITHUB, API_DOCS, ERROR_MESSAGES, PR_REVIEW)
}

/** Phase thresholds (total mastered phrases) for promotion. */
object Phases {
    const val PHASE_2_THRESHOLD = 80
    const val PHASE_3_THRESHOLD = 200

    fun phaseFor(totalPhrases: Int): Int = when {
        totalPhrases >= PHASE_3_THRESHOLD -> 3
        totalPhrases >= PHASE_2_THRESHOLD -> 2
        else -> 1
    }

    fun progressInPhase(totalPhrases: Int): Float = when (phaseFor(totalPhrases)) {
        1 -> (totalPhrases.toFloat() / PHASE_2_THRESHOLD).coerceIn(0f, 1f)
        2 -> ((totalPhrases - PHASE_2_THRESHOLD).toFloat() /
            (PHASE_3_THRESHOLD - PHASE_2_THRESHOLD)).coerceIn(0f, 1f)
        else -> 1f
    }
}

class ReadItRepository(
    private val context: Context,
    private val db: AppDatabase,
) {
    val phraseDao = db.phraseDao()
    private val flashcardDao = db.flashcardDao()
    private val quizDao = db.quizResultDao()
    private val studyLogDao = db.studyLogDao()
    private val progressDao = db.userProgressDao()

    // ---- seeding ----------------------------------------------------------
    suspend fun ensureSeeded() {
        if (phraseDao.count() == 0) {
            phraseDao.insertAll(PhraseSeed.load(context))
        }
        if (progressDao.get() == null) {
            progressDao.upsert(
                UserProgress(phaseStartDate = System.currentTimeMillis()),
            )
        }
    }

    // ---- phrases / dictionary --------------------------------------------
    fun allPhrases(): Flow<List<Phrase>> = phraseDao.observeAll()
    fun phrasesByCategory(c: String): Flow<List<Phrase>> = phraseDao.observeByCategory(c)
    fun search(q: String): Flow<List<Phrase>> = phraseDao.search(q)
    fun favorites(): Flow<List<Phrase>> = phraseDao.observeFavorites()
    fun phrase(id: Long): Flow<Phrase?> = phraseDao.observeById(id)
    fun categoryCounts(): Flow<List<CategoryCount>> = phraseDao.observeCategoryCounts()

    suspend fun toggleFavorite(id: Long) {
        val current = phraseDao.getById(id) ?: return
        phraseDao.setFavorite(id, !current.isFavorite)
    }

    // ---- flashcards / SRS -------------------------------------------------
    fun dueCount(): Flow<Int> = flashcardDao.observeDueCount(System.currentTimeMillis())
    fun studiedCount(): Flow<Int> = flashcardDao.observeStudiedCount()

    /** Returns due cards' phrases, topping up with new phrases when few are due. */
    suspend fun reviewQueue(limit: Int = 20): List<Phrase> {
        val now = System.currentTimeMillis()
        val due = flashcardDao.due(now, limit)
        val phrases = phraseDao.getByIds(due.map { it.phraseId }).toMutableList()
        if (phrases.size < limit) {
            val have = phrases.map { it.id }.toSet()
            phraseDao.randomPhrases(limit - phrases.size)
                .filter { it.id !in have }
                .forEach { phrases.add(it) }
        }
        return phrases
    }

    suspend fun rateCard(phraseId: Long, rating: Sm2Rating) {
        val now = System.currentTimeMillis()
        val card = flashcardDao.getByPhrase(phraseId)
            ?: Flashcard(phraseId = phraseId, dueDate = now)
        flashcardDao.upsert(Sm2.schedule(card, rating, now))
    }

    // ---- quiz -------------------------------------------------------------
    suspend fun quizPhrases(n: Int): List<Phrase> = phraseDao.randomPhrases(n)
    fun recentQuizzes(limit: Int = 20): Flow<List<QuizResult>> = quizDao.recent(limit)

    suspend fun saveQuizResult(mode: String, score: Int, total: Int, weak: List<Long>) {
        quizDao.insert(
            QuizResult(
                quizMode = mode,
                score = score,
                total = total,
                weakPhraseIds = weak,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun phrasesByIds(ids: List<Long>): List<Phrase> = phraseDao.getByIds(ids)

    // ---- progress / study logs -------------------------------------------
    fun progress(): Flow<UserProgress?> = progressDao.observe()
    fun recentLogs(limit: Int = 14): Flow<List<StudyLog>> = studyLogDao.recent(limit)

    /** Records study activity for today and recomputes streak / phase. */
    suspend fun recordStudy(durationMin: Int, phrasesStudied: Int) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone).toString()
        val existing = studyLogDao.getByDate(today)
        val progress = progressDao.get() ?: UserProgress()

        studyLogDao.upsert(
            StudyLog(
                date = today,
                durationMin = (existing?.durationMin ?: 0) + durationMin,
                phrasesStudied = (existing?.phrasesStudied ?: 0) + phrasesStudied,
                phase = progress.currentPhase,
            ),
        )

        val newTotal = progress.totalPhrases + phrasesStudied
        val streak = when (progress.lastStudyDate) {
            today -> progress.streakDays.coerceAtLeast(1)
            LocalDate.now(zone).minusDays(1).toString() -> progress.streakDays + 1
            "" -> 1
            else -> {
                val last = runCatching { LocalDate.parse(progress.lastStudyDate) }.getOrNull()
                if (last != null && ChronoUnit.DAYS.between(last, LocalDate.now(zone)) == 1L) {
                    progress.streakDays + 1
                } else {
                    1
                }
            }
        }
        progressDao.upsert(
            progress.copy(
                totalPhrases = newTotal,
                currentPhase = Phases.phaseFor(newTotal),
                streakDays = streak,
                lastStudyDate = today,
            ),
        )
    }
}
