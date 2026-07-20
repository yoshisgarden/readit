package com.yoshisgarden.readit.ui.components

import androidx.compose.ui.graphics.Color

/**
 * 正解 / 不正解 colors, shared by the quiz and the flashcard choice mode.
 *
 * Deliberately fixed rather than theme-derived: the themes are often pink, and
 * right-vs-wrong has to read at a glance without depending on which one is active.
 */
val CorrectGreen = Color(0xFF2E7D32)
val CorrectContainer = Color(0xFFC8E6C9)
val WrongRed = Color(0xFFC62828)
val WrongContainer = Color(0xFFFFCDD2)
