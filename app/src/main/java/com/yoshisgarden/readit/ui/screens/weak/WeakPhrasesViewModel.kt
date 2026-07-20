package com.yoshisgarden.readit.ui.screens.weak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshisgarden.readit.data.ReadItRepository
import com.yoshisgarden.readit.data.WeakPhrase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WeakPhrasesViewModel(repo: ReadItRepository) : ViewModel() {

    val phrases: StateFlow<List<WeakPhrase>> = repo.weakPhrases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        /** Cap for the "review these" button — one session's worth. */
        const val REVIEW_BATCH = 20
    }
}
