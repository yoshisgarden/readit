package com.yoshisgarden.readit.ui.screens.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yoshisgarden.readit.notif.ReminderScheduler
import com.yoshisgarden.readit.ui.theme.AppTheme
import com.yoshisgarden.readit.ui.theme.DarkModePref
import com.yoshisgarden.readit.ui.theme.colorSchemeFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onBack: () -> Unit,
) {
    val s by vm.settings.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // ---- Theme ----
            SectionTitle("テーマカラー")
            Text(
                "パステル系（かわいい）とシック・クール系（かっこいい）から選べます。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            AppTheme.entries.forEach { theme ->
                ThemeRow(
                    theme = theme,
                    selected = s.theme == theme,
                    onClick = { vm.setTheme(theme) },
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))
            SectionTitle("表示モード")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DarkModePref.entries.forEach { mode ->
                    FilterChip(
                        selected = s.darkMode == mode,
                        onClick = { vm.setDarkMode(mode) },
                        label = { Text(darkLabel(mode)) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            ToggleRow(
                title = "ダイナミックカラー",
                subtitle = "壁紙連動色（Android 12+）。ONにするとテーマより優先されます。",
                checked = s.dynamicColor,
                onCheckedChange = { vm.setDynamicColor(it) },
            )

            Spacer(Modifier.height(20.dp))
            SectionTitle("通知")
            ToggleRow(
                title = "毎日のリマインダー",
                subtitle = "毎日 %02d:%02d に学習をお知らせします".format(s.reminderHour, s.reminderMinute),
                checked = s.reminderEnabled,
                onCheckedChange = { enabled ->
                    vm.setReminder(enabled, s.reminderHour, s.reminderMinute)
                    if (enabled) ReminderScheduler.schedule(context, s.reminderHour, s.reminderMinute)
                    else ReminderScheduler.cancel(context)
                },
            )
            if (s.reminderEnabled) {
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                vm.setReminder(true, h, m)
                                ReminderScheduler.schedule(context, h, m)
                            },
                            s.reminderHour, s.reminderMinute, true,
                        ).show()
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text("時刻を変更（%02d:%02d）".format(s.reminderHour, s.reminderMinute)) }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("学習目標")
            Text(
                "1日の学習時間の目標です。進捗グラフに赤い目標ラインとして表示されます。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15, 30).forEach { g ->
                    FilterChip(
                        selected = s.dailyGoalMin == g,
                        onClick = { vm.setDailyGoal(g) },
                        label = { Text("${g}分") },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "使い方・バージョン情報・開発支援は、メニューの「ヘルプ」にあります。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThemeRow(theme: AppTheme, selected: Boolean, onClick: () -> Unit) {
    val scheme = colorSchemeFor(theme, dark = false)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Swatch(scheme.primary)
                Swatch(scheme.secondary)
                Swatch(scheme.tertiary)
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(theme.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    theme.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Icon(Icons.Filled.Check, "選択中", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(
        Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun darkLabel(mode: DarkModePref) = when (mode) {
    DarkModePref.SYSTEM -> "端末に従う"
    DarkModePref.LIGHT -> "ライト"
    DarkModePref.DARK -> "ダーク"
}
