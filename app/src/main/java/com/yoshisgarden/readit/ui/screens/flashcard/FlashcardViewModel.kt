package com.yoshisgarden.readit.ui.screens.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshisgarden.readit.data.FlashcardMode
import com.yoshisgarden.readit.data.Phrase
import com.yoshisgarden.readit.data.ReadItRepository
import com.yoshisgarden.readit.data.SettingsStore
import com.yoshisgarden.readit.srs.Sm2Rating
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

/**
 * The 3-choice question for the current card in [FlashcardMode.CHOICE].
 *
 * [revealed] holds the indices the user has flipped so far. Wrong picks stay
 * revealed (showing ✕ and what that phrase actually means) while the user keeps
 * trying, so a miss teaches three phrases instead of one.
 */
data class ChoiceState(
    val options: List<Phrase>,
    val correctIndex: Int,
    val revealed: Set<Int> = emptySet(),
    val attempts: Int = 0,
    val solved: Boolean = false,
) {
    /** Rating for the whole question: only a first-try hit counts as known. */
    val rating: Sm2Rating
        get() = if (attempts <= 1) Sm2Rating.KNOWN else Sm2Rating.UNKNOWN
}

data class FlashcardUiState(
    val loading: Boolean = true,
    val mode: FlashcardMode = FlashcardMode.FLIP,
    val queue: List<QueueCard> = emptyList(),
    val index: Int = 0,
    val flipped: Boolean = false,
    val choice: ChoiceState? = null,
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

class FlashcardViewModel(
    private val repo: ReadItRepository,
    private val settings: SettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(FlashcardUiState())
    val state: StateFlow<FlashcardUiState> = _state.asStateFlow()

    private var sessionId = 0L
    private var distractors: List<Phrase> = emptyList()

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
            val mode = settings.settings.first().flashcardMode
            if (mode == FlashcardMode.CHOICE) distractors = repo.distractorPool()

            val queue = q.review.map { QueueCard(it, CardPhase.REVIEW) } +
                q.fresh.map { QueueCard(it, CardPhase.NEW) }
            _state.value = FlashcardUiState(
                loading = false,
                mode = mode,
                queue = queue,
                choice = queue.firstOrNull()?.let { buildChoice(it.phrase, mode) },
                direction = direction,
                reviewTotal = q.review.size,
                newTotal = q.fresh.size,
                finished = q.isEmpty,
            )
        }
    }

    fun setMode(mode: FlashcardMode) {
        viewModelScope.launch { settings.setFlashcardMode(mode) }
        val s = _state.value
        if (mode == s.mode) return
        viewModelScope.launch {
            if (mode == FlashcardMode.CHOICE && distractors.isEmpty()) {
                distractors = repo.distractorPool()
            }
            // Switching restarts the current card only; the queue and everything
            // already answered stay put.
            val cur = _state.value
            _state.value = cur.copy(
                mode = mode,
                flipped = false,
                choice = cur.current?.let { buildChoice(it.phrase, mode) },
            )
        }
    }

    fun flip() { _state.value = _state.value.copy(flipped = !_state.value.flipped) }

    fun setDirection(d: CardDirection) {
        _state.value = _state.value.copy(direction = d, flipped = false)
    }

    /** Flip-mode answer: the user rates their own recall, then the card advances. */
    fun rate(rating: Sm2Rating) {
        val s = _state.value
        val card = s.queue.getOrNull(s.index) ?: return
        viewModelScope.launch { repo.rateCard(card.phrase.id, rating, sessionId) }
        _state.value = commit(s, card, rating)
    }

    /** Choice-mode answer: reveals the tapped option and, once solved, records it. */
    fun selectChoice(option: Int) {
        val s = _state.value
        val choice = s.choice ?: return
        val card = s.queue.getOrNull(s.index) ?: return
        if (choice.solved || option in choice.revealed) return

        val updated = choice.copy(
            revealed = choice.revealed + option,
            attempts = choice.attempts + 1,
            solved = option == choice.correctIndex,
        )
        _state.value = s.copy(choice = updated)

        // The rating is settled the moment the question is solved: a first-try hit is
        // 知ってる, anything else 知らない. Recording only on solve keeps it to one
        // answer per card, no matter how many wrong options were opened.
        if (updated.solved) {
            viewModelScope.launch { repo.rateCard(card.phrase.id, updated.rating, sessionId) }
        }
    }

    /** Choice-mode: move on after the answer has been revealed. */
    fun nextCard() {
        val s = _state.value
        val choice = s.choice ?: return
        val card = s.queue.getOrNull(s.index) ?: return
        if (!choice.solved) return
        _state.value = commit(s, card, choice.rating)
    }

    /**
     * Applies an answer to the queue: re-inserts a missed card, bumps the counters
     * and moves to the next card. Shared by both modes so they cannot drift.
     */
    private fun commit(s: FlashcardUiState, card: QueueCard, rating: Sm2Rating): FlashcardUiState {
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
            return s.copy(
                queue = queue,
                reviewedCount = reviewed,
                retryCount = retries,
                finished = true,
            )
        }
        val next = s.index + 1
        return s.copy(
            queue = queue,
            index = next,
            flipped = false,
            choice = buildChoice(queue[next].phrase, s.mode),
            reviewedCount = reviewed,
            retryCount = retries,
        )
    }

    /**
     * Picks 2 distractors for [answer], preferring its own category so the wrong
     * options are plausible rather than obviously off-topic.
     */
    private fun buildChoice(answer: Phrase, mode: FlashcardMode): ChoiceState? {
        if (mode != FlashcardMode.CHOICE) return null
        val pool = distractors.filter { it.id != answer.id && it.japanese != answer.japanese }
        val sameCategory = pool.filter { it.category == answer.category }
        val picked = sameCategory.ifEmpty { pool }.shuffled().take(2)
        if (picked.size < 2) return null
        val options = (picked + answer).shuffled()
        return ChoiceState(options = options, correctIndex = options.indexOf(answer))
    }

    private companion object {
        const val SESSION_SIZE = 20

        /** Cards to skip before a missed card comes back around. */
        const val RETRY_GAP = 5

        /** Cap so repeatedly missing a card can't make the session endless. */
        const val MAX_RETRIES = 2
    }
}
