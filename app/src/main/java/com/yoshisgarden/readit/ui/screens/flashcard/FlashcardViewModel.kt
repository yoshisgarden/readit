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

data class FlashcardUiState(
    val loading: Boolean = true,
    val cards: List<Phrase> = emptyList(),
    val index: Int = 0,
    val flipped: Boolean = false,
    val direction: CardDirection = CardDirection.EN_TO_JA,
    val reviewedCount: Int = 0,
    val finished: Boolean = false,
)

class FlashcardViewModel(private val repo: ReadItRepository) : ViewModel() {

    private val _state = MutableStateFlow(FlashcardUiState())
    val state: StateFlow<FlashcardUiState> = _state.asStateFlow()

    fun load(direction: CardDirection = CardDirection.EN_TO_JA) {
        _state.value = FlashcardUiState(loading = true, direction = direction)
        viewModelScope.launch {
            val cards = repo.reviewQueue(20)
            _state.value = FlashcardUiState(
                loading = false,
                cards = cards,
                direction = direction,
                finished = cards.isEmpty(),
            )
        }
    }

    fun flip() { _state.value = _state.value.copy(flipped = !_state.value.flipped) }

    fun setDirection(d: CardDirection) { _state.value = _state.value.copy(direction = d, flipped = false) }

    fun rate(rating: Sm2Rating) {
        val s = _state.value
        val card = s.cards.getOrNull(s.index) ?: return
        viewModelScope.launch { repo.rateCard(card.id, rating) }

        if (s.index + 1 >= s.cards.size) {
            viewModelScope.launch { repo.recordStudy(durationMin = 5, phrasesStudied = s.reviewedCount + 1) }
            _state.value = s.copy(reviewedCount = s.reviewedCount + 1, finished = true)
        } else {
            _state.value = s.copy(index = s.index + 1, flipped = false, reviewedCount = s.reviewedCount + 1)
        }
    }
}
