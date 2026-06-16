package com.yoshisgarden.readit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** Dark-mode preference. */
enum class DarkModePref { SYSTEM, LIGHT, DARK }

@Composable
fun ReadItTheme(
    appTheme: AppTheme = AppTheme.SAKURA,
    darkMode: DarkModePref = DarkModePref.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (darkMode) {
        DarkModePref.SYSTEM -> isSystemInDarkTheme()
        DarkModePref.LIGHT -> false
        DarkModePref.DARK -> true
    }

    val context = LocalContext.current
    val scheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        else -> colorSchemeFor(appTheme, dark)
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        content = content,
    )
}

fun colorSchemeFor(theme: AppTheme, dark: Boolean): ColorScheme = when (theme) {
    AppTheme.SAKURA -> if (dark) SakuraDark else SakuraLight
    AppTheme.MINT -> if (dark) MintDark else MintLight
    AppTheme.CHARCOAL -> if (dark) CharcoalDark else CharcoalLight
    AppTheme.INDIGO -> if (dark) IndigoDark else IndigoLight
}
