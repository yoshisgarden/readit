package com.yoshisgarden.readit.ui.screens.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshisgarden.readit.data.Phrase
import com.yoshisgarden.readit.data.ReadItRepository
import com.yoshisgarden.readit.srs.Sm2Rating
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CardDirection { EN_TO_JA, JA_TO_EN }

/** Which block of the session a card belongs to — drives the header label. */
enum class CardPhase { REVIEW, NEW, RETRY }

data class QueueCard(
    val phrase: Phrase,
    val phase: CardPhase,
    /** How many times this card has already been re-shown in this session. */
    val retry: Int = 0,
)

data class FlashcardUiState(
    val loading: Boolean = true,
    val queue: List<QueueCard> = emptyList(),
    val index: Int = 0,
    val flipped: Boolean = false,
    val direction: CardDirection = CardDirection.EN_TO_JA,
    /** Cards finished for good — retries don't count until the card stops coming back. */
    val reviewedCount: Int = 0,
    val retryCount: Int = 0,
    val reviewTotal: Int = 0,
    val newTotal: Int = 0,
    val finished: Boolean = false,
) {
    val current: QueueCard? get() = queue.getOrNull(index)

    /** e.g. "前回の復習 2/5". Retries are labelled without a counter. */
    val phaseLabel: String
        get() = when (current?.phase) {
            CardPhase.REVIEW -> "前回の復習 ${countUpTo(CardPhase.REVIEW)}/$reviewTotal"
            CardPhase.NEW -> "今日の学習 ${countUpTo(CardPhase.NEW)}/$newTotal"
            CardPhase.RETRY -> "もう一度"
            null -> ""
        }

    private fun countUpTo(phase: CardPhase): Int =
        queue.take(index + 1).count { it.phase == phase }
}

class FlashcardViewModel(private val repo: ReadItRepository) : ViewModel() {

    private val _state = MutableStateFlow(FlashcardUiState())
    val state: StateFlow<FlashcardUiState> = _state.asStateFlow()

    private var sessionId = 0L

    /**
     * Loads a session. [phraseIds] runs a focused review of an explicit list (the
     * weak-phrase screen); otherwise the repository builds the normal queue.
     */
    fun load(
        direction: CardDirection = CardDirection.EN_TO_JA,
        phraseIds: List<Long>? = null,
    ) {
        _state.value = FlashcardUiState(loading = true, direction = direction)
        viewModelScope.launch {
            // Read the queue before the first answer is logged — the review block is
            // derived from the latest session id, which our own writes would become.
            val q = if (phraseIds.isNullOrEmpty()) repo.reviewQueue(SESSION_SIZE)
            else repo.queueFromIds(phraseIds)
            sessionId = repo.newSessionId()

            _state.value = FlashcardUiState(
                loading = false,
                queue = q.review.map { QueueCard(it, CardPhase.REVIEW) } +
                    q.fresh.map { QueueCard(it, CardPhase.NEW) },
                direction = direction,
                reviewTotal = q.review.size,
                newTotal = q.fresh.size,
                finished = q.isEmpty,
            )
        }
    }

    fun flip() { _state.value = _state.value.copy(flipped = !_state.value.flipped) }

    fun setDirection(d: CardDirection) {
        _state.value = _state.value.copy(direction = d, flipped = false)
    }

    fun rate(rating: Sm2Rating) {
        val s = _state.value
        val card = s.queue.getOrNull(s.index) ?: return
        viewModelScope.launch { repo.rateCard(card.phrase.id, rating, sessionId) }

        // "知らない" puts the card back into this session instead of ending it there.
        // Spacing it a few cards out makes the re-show a real recall test rather than
        // an echo of the answer still on screen.
        val comesBack = rating == Sm2Rating.UNKNOWN && card.retry < MAX_RETRIES
        val queue = if (comesBack) {
            s.queue.toMutableList().apply {
                val at = (s.index + RETRY_GAP).coerceAtMost(size)
                add(at, card.copy(phase = CardPhase.RETRY, retry = card.retry + 1))
            }
        } else {
            s.queue
        }

        val reviewed = if (comesBack) s.reviewedCount else s.reviewedCount + 1
        val retries = if (comesBack) s.retryCount + 1 else s.retryCount

        if (s.index + 1 >= queue.size) {
            viewModelScope.launch {
                repo.recordStudy(durationMin = 5, phrasesStudied = reviewed)
            }
            _state.value = s.copy(
                queue = queue,
                reviewedCount = reviewed,
                retryCount = retries,
                finished = true,
            )
        } else {
            _state.value = s.copy(
                queue = queue,
                index = s.index + 1,
                flipped = false,
                reviewedCount = reviewed,
                retryCount = retries,
            )
        }
    }

    private companion object {
        const val SESSION_SIZE = 20

        /** Cards to skip before a missed card comes back around. */
        const val RETRY_GAP = 5

        /** Cap so repeatedly pressing 知らない can't make the session endless. */
        const val MAX_RETRIES = 2
    }
}
