package com.yoshisgarden.readit.ui.screens.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshisgarden.readit.data.CategoryCount
import com.yoshisgarden.readit.data.Phrase
import com.yoshisgarden.readit.data.ReadItRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** null category = all; "★" sentinel handled via [showFavorites]. */
data class DictionaryFilter(
    val query: String = "",
    val category: String? = null,
    val favoritesOnly: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DictionaryViewModel(private val repo: ReadItRepository) : ViewModel() {

    private val _filter = MutableStateFlow(DictionaryFilter())
    val filter: StateFlow<DictionaryFilter> = _filter

    val categoryCounts: StateFlow<List<CategoryCount>> =
        repo.categoryCounts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val phrases: StateFlow<List<Phrase>> =
        _filter.flatMapLatest { f ->
            when {
                f.query.isNotBlank() -> repo.search(f.query.trim())
                f.favoritesOnly -> repo.favorites()
                f.category != null -> repo.phrasesByCategory(f.category)
                else -> repo.allPhrases()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { _filter.value = _filter.value.copy(query = q) }
    fun setCategory(c: String?) {
        _filter.value = _filter.value.copy(category = c, favoritesOnly = false, query = "")
    }
    fun setFavoritesOnly(on: Boolean) {
        _filter.value = _filter.value.copy(favoritesOnly = on, category = null, query = "")
    }

    fun phrase(id: Long): Flow<Phrase?> = repo.phrase(id)

    fun toggleFavorite(id: Long) {
        viewModelScope.launch { repo.toggleFavorite(id) }
    }
}
