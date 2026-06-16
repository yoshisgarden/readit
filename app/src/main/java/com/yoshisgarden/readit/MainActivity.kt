package com.yoshisgarden.readit

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yoshisgarden.readit.ui.ReadItRoot
import com.yoshisgarden.readit.ui.ReadItViewModels
import com.yoshisgarden.readit.ui.screens.settings.SettingsViewModel
import com.yoshisgarden.readit.ui.theme.ReadItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
        setContent {
            val settingsVm: SettingsViewModel = viewModel(factory = ReadItViewModels.Factory)
            val settings by settingsVm.settings.collectAsState()
            ReadItTheme(
                appTheme = settings.theme,
                darkMode = settings.darkMode,
                dynamicColor = settings.dynamicColor,
            ) {
                Surface { ReadItRoot() }
            }
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
