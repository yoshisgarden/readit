package com.yoshisgarden.readit

import android.content.Context
import com.yoshisgarden.readit.data.AppDatabase
import com.yoshisgarden.readit.data.ReadItRepository
import com.yoshisgarden.readit.data.SettingsStore

/** Tiny manual DI container (no Hilt) — created once in [ReadItApp]. */
class AppContainer(context: Context) {
    private val db = AppDatabase.get(context)
    val repository = ReadItRepository(context.applicationContext, db)
    val settingsStore = SettingsStore(context.applicationContext)
}
