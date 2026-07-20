package com.yoshisgarden.readit.srs

import com.yoshisgarden.readit.data.Flashcard

/**
 * SM-2 spaced-repetition scheduler.
 *
 * The flashcard UI exposes 3 buttons; they map to SM-2 quality scores:
 *   UNKNOWN ("知らない") -> q = 1  (reset)
 *   VAGUE   ("うっすら") -> q = 3
 *   KNOWN   ("知ってる") -> q = 5
 */
enum class Sm2Rating(val quality: Int, val storedValue: Int) {
    UNKNOWN(1, 0),
    VAGUE(3, 1),
    KNOWN(5, 2),
}

object Sm2 {
    private const val DAY_MS = 24L * 60 * 60 * 1000
    private const val MIN_EF = 1.3

    /**
     * Returns an updated flashcard scheduled relative to [now] (epoch millis).
     *
     * [isRetry] marks a second (or later) answer for the same card *within one
     * session*, produced by the "知らない" re-show. Such an answer must not walk the
     * SM-2 ladder: counting it as another review would send a card the user just
     * missed from 1 day straight to 6, and it would penalise the ease twice. A retry
     * only re-confirms the card is still being learned, so it stays due tomorrow.
     */
    fun schedule(
        card: Flashcard,
        rating: Sm2Rating,
        now: Long,
        isRetry: Boolean = false,
    ): Flashcard {
        if (isRetry) {
            return card.copy(
                interval = 1,
                dueDate = now + DAY_MS,
                rating = rating.storedValue,
            )
        }

        val q = rating.quality
        var ef = card.easeFactor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        if (ef < MIN_EF) ef = MIN_EF

        val newInterval: Int
        if (q < 3) {
            // Failed recall: restart the ladder.
            newInterval = 1
        } else {
            newInterval = when (card.reviewCount) {
                0 -> 1
                1 -> 6
                else -> Math.round(card.interval * ef).toInt().coerceAtLeast(1)
            }
        }

        return card.copy(
            interval = newInterval,
            easeFactor = ef,
            dueDate = now + newInterval * DAY_MS,
            rating = rating.storedValue,
            reviewCount = card.reviewCount + 1,
        )
    }
}
