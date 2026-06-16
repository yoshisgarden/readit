package com.yoshisgarden.readit.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Four selectable palettes:
 *   SAKURA / MINT  -> cute pastel (designed to be fun to use)
 *   CHARCOAL       -> chic, understated
 *   INDIGO         -> cool / masculine
 */
enum class AppTheme(val id: String, val displayName: String, val tagline: String) {
    SAKURA("sakura", "さくらパステル", "やさしいピンク × ラベンダー"),
    MINT("mint", "ミントパステル", "さわやかなミント × アクア"),
    CHARCOAL("charcoal", "シック", "落ち着いたスレート × ベージュ"),
    INDIGO("indigo", "クール", "インディゴ × シアン");

    companion object {
        fun fromId(id: String?): AppTheme = entries.firstOrNull { it.id == id } ?: SAKURA
    }
}

// ---- Sakura (pastel) ----
val SakuraLight = lightColorScheme(
    primary = Color(0xFFD15B8C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E6),
    onPrimaryContainer = Color(0xFF3E0021),
    secondary = Color(0xFF8B6FD0),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE9DDFF),
    onSecondaryContainer = Color(0xFF26124F),
    tertiary = Color(0xFFEBA13C),
    background = Color(0xFFFFF8FB),
    onBackground = Color(0xFF241A1E),
    surface = Color(0xFFFFF8FB),
    onSurface = Color(0xFF241A1E),
    surfaceVariant = Color(0xFFF6E3EC),
    onSurfaceVariant = Color(0xFF6E5660),
    outline = Color(0xFFA08490),
)
val SakuraDark = darkColorScheme(
    primary = Color(0xFFFFB0CC),
    onPrimary = Color(0xFF5E1138),
    primaryContainer = Color(0xFF7C2C50),
    onPrimaryContainer = Color(0xFFFFD9E6),
    secondary = Color(0xFFCEBDFF),
    onSecondary = Color(0xFF3A2466),
    secondaryContainer = Color(0xFF52397E),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = Color(0xFFFFC987),
    background = Color(0xFF1E161A),
    onBackground = Color(0xFFEDDFE5),
    surface = Color(0xFF1E161A),
    onSurface = Color(0xFFEDDFE5),
    surfaceVariant = Color(0xFF514349),
    onSurfaceVariant = Color(0xFFD6C2CB),
    outline = Color(0xFF9E8C94),
)

// ---- Mint (pastel) ----
val MintLight = lightColorScheme(
    primary = Color(0xFF1C9C86),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA8F2E0),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4FA3D8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE7FA),
    onSecondaryContainer = Color(0xFF06283B),
    tertiary = Color(0xFFE39AB8),
    background = Color(0xFFF4FCF9),
    onBackground = Color(0xFF18211E),
    surface = Color(0xFFF4FCF9),
    onSurface = Color(0xFF18211E),
    surfaceVariant = Color(0xFFD9E8E2),
    onSurfaceVariant = Color(0xFF4E6259),
    outline = Color(0xFF7E928A),
)
val MintDark = darkColorScheme(
    primary = Color(0xFF66D9C2),
    onPrimary = Color(0xFF003730),
    primaryContainer = Color(0xFF005046),
    onPrimaryContainer = Color(0xFFA8F2E0),
    secondary = Color(0xFFA6CFEC),
    onSecondary = Color(0xFF0B3550),
    secondaryContainer = Color(0xFF294C66),
    onSecondaryContainer = Color(0xFFCDE7FA),
    tertiary = Color(0xFFF4B6CE),
    background = Color(0xFF101513),
    onBackground = Color(0xFFDFE8E3),
    surface = Color(0xFF101513),
    onSurface = Color(0xFFDFE8E3),
    surfaceVariant = Color(0xFF3F4945),
    onSurfaceVariant = Color(0xFFBFC9C3),
    outline = Color(0xFF89938E),
)

// ---- Charcoal (chic) ----
val CharcoalLight = lightColorScheme(
    primary = Color(0xFF4F5B6B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8DEE7),
    onPrimaryContainer = Color(0xFF111923),
    secondary = Color(0xFF8A7B6B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDE0D2),
    onSecondaryContainer = Color(0xFF2A2015),
    tertiary = Color(0xFF6E7F73),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1C1E),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE3E3E6),
    onSurfaceVariant = Color(0xFF55585E),
    outline = Color(0xFF85888E),
)
val CharcoalDark = darkColorScheme(
    primary = Color(0xFFB4C0D2),
    onPrimary = Color(0xFF1E2935),
    primaryContainer = Color(0xFF35414F),
    onPrimaryContainer = Color(0xFFD8DEE7),
    secondary = Color(0xFFD5C5B3),
    onSecondary = Color(0xFF3B2F22),
    secondaryContainer = Color(0xFF534637),
    onSecondaryContainer = Color(0xFFEDE0D2),
    tertiary = Color(0xFFB4C5B8),
    background = Color(0xFF15161A),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF15161A),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6CD),
    outline = Color(0xFF8E9098),
)

// ---- Indigo (cool) ----
val IndigoLight = lightColorScheme(
    primary = Color(0xFF3F51C5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE1FF),
    onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFF1A8CB0),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC2EAFA),
    onSecondaryContainer = Color(0xFF001F2A),
    tertiary = Color(0xFF6E5BC0),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF1A1B22),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF1A1B22),
    surfaceVariant = Color(0xFFE2E1EC),
    onSurfaceVariant = Color(0xFF45464F),
    outline = Color(0xFF767680),
)
val IndigoDark = darkColorScheme(
    primary = Color(0xFFB9C3FF),
    onPrimary = Color(0xFF0A1C8C),
    primaryContainer = Color(0xFF2636AC),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFF8AD2EC),
    onSecondary = Color(0xFF003545),
    secondaryContainer = Color(0xFF004C62),
    onSecondaryContainer = Color(0xFFC2EAFA),
    tertiary = Color(0xFFD0BCFF),
    background = Color(0xFF121319),
    onBackground = Color(0xFFE3E1EC),
    surface = Color(0xFF121319),
    onSurface = Color(0xFFE3E1EC),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC6C5D0),
    outline = Color(0xFF90909A),
)
