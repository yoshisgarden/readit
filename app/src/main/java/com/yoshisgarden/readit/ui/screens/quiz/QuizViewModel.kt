package com.yoshisgarden.readit.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshisgarden.readit.data.Categories
import com.yoshisgarden.readit.data.Phrase
import com.yoshisgarden.readit.data.ReadItRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class QuizMode(val id: String, val title: String, val description: String) {
    FILL_BLANK("fill_blank", "穴埋め", "メッセージ文の空欄に入るフレーズを選ぶ"),
    ERROR_ANALYSIS("error_analysis", "エラー解析", "このエラーの意味は？を4択で"),
    DOC_READING("doc_reading", "読解", "短い英文の意味を選ぶ"),
}

data class QuizQuestion(
    val phraseId: Long,
    val prompt: String,
    val promptMono: Boolean,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val hint: String = "",
)

data class QuizUiState(
    val mode: QuizMode = QuizMode.FILL_BLANK,
    val loading: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val index: Int = 0,
    val selected: Int? = null,
    val answered: Boolean = false,
    val correctCount: Int = 0,
    val weakPhraseIds: List<Long> = emptyList(),
    val finished: Boolean = false,
)

class QuizViewModel(private val repo: ReadItRepository) : ViewModel() {

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    fun start(mode: QuizMode, count: Int = 6) {
        _state.value = QuizUiState(mode = mode, loading = true)
        viewModelScope.launch {
            val pool = when (mode) {
                QuizMode.ERROR_ANALYSIS ->
                    repo.phrasesByCategory(Categories.ERROR_MESSAGES).let { repo.quizPhrases(40) }
                else -> repo.quizPhrases(40)
            }
            val distractPool = repo.quizPhrases(40)
            val questions = pool.shuffled().take(count).map { buildQuestion(mode, it, distractPool) }
            _state.value = QuizUiState(mode = mode, questions = questions)
        }
    }

    private fun buildQuestion(mode: QuizMode, p: Phrase, pool: List<Phrase>): QuizQuestion {
        return when (mode) {
            QuizMode.FILL_BLANK -> {
                val blanked = if (p.exampleEn.contains(p.english, ignoreCase = true)) {
                    p.exampleEn.replace(p.english, "______", ignoreCase = true)
                } else {
                    "______ — ${p.exampleJa}"
                }
                val opts = optionsFrom(p.english, pool.map { it.english })
                QuizQuestion(
                    phraseId = p.id,
                    prompt = blanked,
                    promptMono = true,
                    options = opts.first,
                    correctIndex = opts.second,
                    explanation = "${p.english} = ${p.japanese}",
                    hint = "ヒント: ${p.japanese}",
                )
            }
            QuizMode.ERROR_ANALYSIS, QuizMode.DOC_READING -> {
                val opts = optionsFrom(p.japanese, pool.map { it.japanese })
                QuizQuestion(
                    phraseId = p.id,
                    prompt = if (mode == QuizMode.ERROR_ANALYSIS) p.exampleEn else p.english,
                    promptMono = true,
                    options = opts.first,
                    correctIndex = opts.second,
                    explanation = "${p.english} = ${p.japanese}\n例: ${p.exampleEn}",
                )
            }
        }
    }

    /** Builds a 4-option list including [correct] and returns (options, correctIndex). */
    private fun optionsFrom(correct: String, others: List<String>): Pair<List<String>, Int> {
        val distractors = others.filter { it != correct }.distinct().shuffled().take(2)
        val all = (distractors + correct).shuffled()
        return all to all.indexOf(correct)
    }

    fun select(i: Int) {
        val s = _state.value
        if (s.answered) return
        val correct = i == s.questions[s.index].correctIndex
        _state.value = s.copy(
            selected = i,
            answered = true,
            correctCount = s.correctCount + if (correct) 1 else 0,
            weakPhraseIds = if (correct) s.weakPhraseIds
            else s.weakPhraseIds + s.questions[s.index].phraseId,
        )
    }

    fun next() {
        val s = _state.value
        if (s.index + 1 >= s.questions.size) {
            _state.value = s.copy(finished = true)
            viewModelScope.launch {
                repo.saveQuizResult(s.mode.id, s.correctCount, s.questions.size, s.weakPhraseIds)
                repo.recordStudy(durationMin = 3, phrasesStudied = s.correctCount)
            }
        } else {
            _state.value = s.copy(index = s.index + 1, selected = null, answered = false)
        }
    }

    fun reset() { _state.value = QuizUiState() }
}
