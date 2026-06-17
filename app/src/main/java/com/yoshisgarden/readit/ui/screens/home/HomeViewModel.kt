package com.yoshisgarden.readit.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshisgarden.readit.data.Phases
import com.yoshisgarden.readit.data.Phrase
import com.yoshisgarden.readit.data.ReadItRepository
import com.yoshisgarden.readit.data.StudyLog
import com.yoshisgarden.readit.data.UserProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val progress: UserProgress = UserProgress(),
    val dueCount: Int = 0,
    val todayLog: StudyLog? = null,
    val phaseProgress: Float = 0f,
    val phaseLabel: String = "Phase 1",
    val phaseGoal: String = "",
    val phraseOfDay: Phrase? = null,
)

class HomeViewModel(private val repo: ReadItRepository) : ViewModel() {

    val state: StateFlow<HomeUiState> =
        combine(
            repo.progress(),
            repo.studyableCount(),
            repo.recentLogs(1),
            repo.allPhrases(),
        ) { progress, due, logs, phrases ->
            val p = progress ?: UserProgress()
            // Pick by *local* epoch-day so it rolls over at local midnight (not UTC 0:00).
            val pod = if (phrases.isEmpty()) null
            else phrases[(repo.todayEpochDay() % phrases.size).toInt()]
            // Only count today's log as "今日の学習" — otherwise the most recent (e.g.
            // yesterday's) row would show under today's label after midnight.
            val today = repo.today()
            HomeUiState(
                progress = p,
                dueCount = due,
                todayLog = logs.firstOrNull()?.takeIf { it.date == today },
                phaseProgress = Phases.progressInPhase(p.totalPhrases),
                phaseLabel = "Phase ${p.currentPhase}",
                phaseGoal = phaseGoal(p),
                phraseOfDay = pod,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun phaseGoal(p: UserProgress): String = when (p.currentPhase) {
        1 -> "Claude Code のメッセージ・エラー文を即読み（あと ${
            (Phases.PHASE_2_THRESHOLD - p.totalPhrases).coerceAtLeast(0)
        } フレーズで Phase 2）"
        2 -> "README・API ドキュメントを辞書なしで理解（あと ${
            (Phases.PHASE_3_THRESHOLD - p.totalPhrases).coerceAtLeast(0)
        } フレーズで Phase 3）"
        else -> "実務直結で読む — 到達おめでとうございます！"
    }
}
