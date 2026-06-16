package com.yoshisgarden.readit

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReadItApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Seed the bundled phrase dictionary on first launch (off the main thread).
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            container.repository.ensureSeeded()
        }
    }
}
