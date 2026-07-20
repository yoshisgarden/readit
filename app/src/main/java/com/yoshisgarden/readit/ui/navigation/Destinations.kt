package com.yoshisgarden.readit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val DICTIONARY = "dictionary"
    const val DETAIL = "detail"            // detail/{phraseId}
    const val QUIZ_SELECT = "quiz_select"
    const val QUIZ = "quiz"                // quiz/{mode}
    const val FLASHCARD = "flashcard"

    /**
     * Focused review of an explicit phrase list (from the weak-phrase screen).
     * Kept separate from [FLASHCARD] so that route stays an exact bottom-tab match.
     */
    const val FLASHCARD_LIST = "flashcard_list"   // flashcard_list/{ids}
    const val WEAK = "weak"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"
    const val HELP = "help"
    const val VERSION = "version"

    fun detail(id: Long) = "$DETAIL/$id"
    fun quiz(mode: String) = "$QUIZ/$mode"
    fun flashcardList(ids: List<Long>) = "$FLASHCARD_LIST/${ids.joinToString(",")}"
}

enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    HOME(Routes.HOME, "ホーム", Icons.Filled.Home),
    DICTIONARY(Routes.DICTIONARY, "辞書", Icons.Outlined.MenuBook),
    QUIZ(Routes.QUIZ_SELECT, "クイズ", Icons.Filled.Quiz),
    FLASHCARD(Routes.FLASHCARD, "単語帳", Icons.Filled.Style),
}
