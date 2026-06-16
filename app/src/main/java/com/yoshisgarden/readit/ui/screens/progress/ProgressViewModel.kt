package com.yoshisgarden.readit.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshisgarden.readit.data.ReadItRepository
import com.yoshisgarden.readit.data.StudyLog
import com.yoshisgarden.readit.data.UserProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProgressUiState(
    val progress: UserProgress = UserProgress(),
    val logs: List<StudyLog> = emptyList(),
    val totalMinutes: Int = 0,
)

class ProgressViewModel(repo: ReadItRepository) : ViewModel() {
    val state: StateFlow<ProgressUiState> =
        combine(repo.progress(), repo.recentLogs(14)) { progress, logs ->
            ProgressUiState(
                progress = progress ?: UserProgress(),
                logs = logs.sortedBy { it.date },
                totalMinutes = logs.sumOf { it.durationMin },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())
}
