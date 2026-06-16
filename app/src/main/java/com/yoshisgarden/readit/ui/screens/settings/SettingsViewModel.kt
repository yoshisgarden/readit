package com.yoshisgarden.readit.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshisgarden.readit.data.AppSettings
import com.yoshisgarden.readit.data.SettingsStore
import com.yoshisgarden.readit.ui.theme.AppTheme
import com.yoshisgarden.readit.ui.theme.DarkModePref
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val store: SettingsStore) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        store.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setTheme(theme: AppTheme) = viewModelScope.launch { store.setTheme(theme) }
    fun setDarkMode(mode: DarkModePref) = viewModelScope.launch { store.setDarkMode(mode) }
    fun setDynamicColor(on: Boolean) = viewModelScope.launch { store.setDynamicColor(on) }
    fun setReminder(enabled: Boolean, hour: Int, minute: Int) =
        viewModelScope.launch { store.setReminder(enabled, hour, minute) }
    fun setDailyGoal(min: Int) = viewModelScope.launch { store.setDailyGoal(min) }
}
