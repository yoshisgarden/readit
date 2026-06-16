package com.yoshisgarden.readit.ui.screens.progress

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yoshisgarden.readit.data.StudyLog
import com.yoshisgarden.readit.ui.components.StatPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    vm: ProgressViewModel,
    onBack: () -> Unit,
) {
    val s by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("進捗グラフ") },
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill("Phase ${s.progress.currentPhase}", "現在のフェーズ", Modifier.weight(1f))
                StatPill("🔥 ${s.progress.streakDays}", "連続日数", Modifier.weight(1f))
                StatPill("${s.totalMinutes}分", "直近の学習", Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            Text("学習時間（直近14日）", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            BarChart(s.logs)
            Spacer(Modifier.height(24.dp))
            Text("習得フレーズ: ${s.progress.totalPhrases}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun BarChart(logs: List<StudyLog>) {
    val max = (logs.maxOfOrNull { it.durationMin } ?: 1).coerceAtLeast(1)
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        if (logs.isEmpty()) {
            Text(
                "まだ学習記録がありません。\n今日の学習を始めましょう！",
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Card
        }
        Column {
        // Scale label: bars are normalized to the largest value (adaptive scale).
        Text(
            "上限 ${max} 分（最大値に合わせて自動調整）",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
        )
        Row(
            Modifier.fillMaxWidth().height(180.dp).padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            logs.takeLast(14).forEach { log ->
                val frac = log.durationMin.toFloat() / max
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    // minutes value above the bar
                    Text(
                        "${log.durationMin}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(2.dp))
                    // fixed-width bar so a single day doesn't fill the whole card
                    Box(
                        Modifier
                            .width(20.dp)
                            .height((120 * frac).dp.coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        log.date.takeLast(2),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
        }
    }
}
