package com.yoshisgarden.readit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.yoshisgarden.readit.ReadItApp
import com.yoshisgarden.readit.ui.screens.dictionary.DictionaryViewModel
import com.yoshisgarden.readit.ui.screens.flashcard.FlashcardViewModel
import com.yoshisgarden.readit.ui.screens.home.HomeViewModel
import com.yoshisgarden.readit.ui.screens.progress.ProgressViewModel
import com.yoshisgarden.readit.ui.screens.quiz.QuizViewModel
import com.yoshisgarden.readit.ui.screens.settings.SettingsViewModel
import com.yoshisgarden.readit.ui.screens.weak.WeakPhrasesViewModel

private fun CreationExtras.app(): ReadItApp =
    (this[APPLICATION_KEY] as ReadItApp)

object ReadItViewModels {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { HomeViewModel(app().container.repository) }
        initializer { DictionaryViewModel(app().container.repository) }
        initializer { QuizViewModel(app().container.repository) }
        initializer {
            FlashcardViewModel(app().container.repository, app().container.settingsStore)
        }
        initializer { ProgressViewModel(app().container.repository, app().container.settingsStore) }
        initializer { SettingsViewModel(app().container.settingsStore) }
        initializer { WeakPhrasesViewModel(app().container.repository) }
    }
}
