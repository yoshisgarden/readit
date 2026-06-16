package com.yoshisgarden.readit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yoshisgarden.readit.ui.theme.AppTheme
import com.yoshisgarden.readit.ui.theme.DarkModePref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val theme: AppTheme = AppTheme.SAKURA,
    val darkMode: DarkModePref = DarkModePref.SYSTEM,
    val dynamicColor: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0,
    val dailyGoalMin: Int = 10,
)

class SettingsStore(private val context: Context) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DARK = stringPreferencesKey("dark_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val REMINDER_ON = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MIN = intPreferencesKey("reminder_minute")
        val DAILY_GOAL = intPreferencesKey("daily_goal_min")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            theme = AppTheme.fromId(p[Keys.THEME]),
            darkMode = runCatching { DarkModePref.valueOf(p[Keys.DARK] ?: "SYSTEM") }
                .getOrDefault(DarkModePref.SYSTEM),
            dynamicColor = p[Keys.DYNAMIC] ?: false,
            reminderEnabled = p[Keys.REMINDER_ON] ?: false,
            reminderHour = p[Keys.REMINDER_HOUR] ?: 21,
            reminderMinute = p[Keys.REMINDER_MIN] ?: 0,
            dailyGoalMin = p[Keys.DAILY_GOAL] ?: 10,
        )
    }

    suspend fun setTheme(theme: AppTheme) =
        context.dataStore.edit { it[Keys.THEME] = theme.id }

    suspend fun setDarkMode(mode: DarkModePref) =
        context.dataStore.edit { it[Keys.DARK] = mode.name }

    suspend fun setDynamicColor(on: Boolean) =
        context.dataStore.edit { it[Keys.DYNAMIC] = on }

    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) =
        context.dataStore.edit {
            it[Keys.REMINDER_ON] = enabled
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MIN] = minute
        }

    suspend fun setDailyGoal(min: Int) =
        context.dataStore.edit { it[Keys.DAILY_GOAL] = min }
}
