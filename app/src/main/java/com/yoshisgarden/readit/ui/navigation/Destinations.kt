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
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"

    fun detail(id: Long) = "$DETAIL/$id"
    fun quiz(mode: String) = "$QUIZ/$mode"
}

enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    HOME(Routes.HOME, "ホーム", Icons.Filled.Home),
    DICTIONARY(Routes.DICTIONARY, "辞書", Icons.Outlined.MenuBook),
    QUIZ(Routes.QUIZ_SELECT, "クイズ", Icons.Filled.Quiz),
    FLASHCARD(Routes.FLASHCARD, "単語帳", Icons.Filled.Style),
}
