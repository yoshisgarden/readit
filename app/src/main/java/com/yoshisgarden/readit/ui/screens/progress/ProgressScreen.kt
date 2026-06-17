package com.yoshisgarden.readit.ui.screens.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
            Spacer(Modifier.height(20.dp))
            GoalCard(
                todayMinutes = s.todayMinutes,
                goalMin = s.dailyGoalMin,
                achievedDays = s.achievedDays,
                recordedDays = s.recordedDays,
            )
            Spacer(Modifier.height(24.dp))
            Text("学習時間（直近14日）", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            BarChart(s.logs, s.dailyGoalMin)
            Spacer(Modifier.height(24.dp))
            Text("習得フレーズ: ${s.progress.totalPhrases}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun GoalCard(
    todayMinutes: Int,
    goalMin: Int,
    achievedDays: Int,
    recordedDays: Int,
) {
    val achieved = todayMinutes >= goalMin
    val remaining = (goalMin - todayMinutes).coerceAtLeast(0)
    val frac = if (goalMin > 0) (todayMinutes.toFloat() / goalMin).coerceIn(0f, 1f) else 1f
    val goalColor = Color(0xFFE53935)
    val doneColor = MaterialTheme.colorScheme.primary

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("今日の目標", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    if (achieved) "達成！🌸" else "あと ${remaining}分",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (achieved) doneColor else goalColor,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { frac },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)),
                color = if (achieved) doneColor else goalColor,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "今日 ${todayMinutes} / ${goalMin}分",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (recordedDays > 0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "直近 ${recordedDays}日のうち ${achievedDays}日 目標達成",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BarChart(logs: List<StudyLog>, goalMin: Int) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        if (logs.isEmpty()) {
            Text(
                "まだ学習記録がありません。\n今日の学習を始めましょう！",
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Card
        }
        val maxVal = logs.maxOf { it.durationMin }
        val scaleMax = niceMax(maxOf(maxVal, goalMin, 1))
        val chartHeight = 160.dp
        val gridValues = listOf(scaleMax, scaleMax / 2, 0)
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        val goalColor = Color(0xFFE53935)
        val goalFrac = (goalMin.toFloat() / scaleMax).coerceIn(0f, 1f)
        val days = logs.takeLast(14)

        Column(Modifier.padding(16.dp)) {
            Text(
                "（分）",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth().height(chartHeight)) {
                // Y axis: minute labels at each gridline
                Box(Modifier.fillMaxHeight().width(28.dp)) {
                    gridValues.forEach { v ->
                        val frac = v.toFloat() / scaleMax
                        Text(
                            "$v",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(y = (chartHeight * (1f - frac) - 9.dp).coerceIn(0.dp, chartHeight - 16.dp))
                                .padding(end = 4.dp),
                        )
                    }
                }
                // Plot area: gridlines + goal line + bars
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    gridValues.forEach { v ->
                        val frac = v.toFloat() / scaleMax
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .align(Alignment.TopStart)
                                .offset(y = chartHeight * (1f - frac))
                                .background(gridColor),
                        )
                    }
                    // red goal line
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.TopStart)
                            .offset(y = chartHeight * (1f - goalFrac))
                            .background(goalColor),
                    )
                    Row(
                        Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        days.forEach { log ->
                            val frac = (log.durationMin.toFloat() / scaleMax).coerceIn(0f, 1f)
                            Box(
                                Modifier.weight(1f).fillMaxHeight(),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                if (log.durationMin > 0) {
                                    Box(
                                        Modifier
                                            .width(18.dp)
                                            .fillMaxHeight(frac.coerceAtLeast(0.02f))
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // X axis: dates aligned under the bars
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(28.dp))
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    days.forEach { log ->
                        Text(
                            log.date.takeLast(2),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "赤線＝目標",
                style = MaterialTheme.typography.bodyMedium,
                color = goalColor,
            )
        }
    }
}

/** Round a max value up to a clean axis maximum. */
private fun niceMax(v: Int): Int = when {
    v <= 5 -> 5
    v <= 10 -> 10
    v <= 20 -> 20
    v <= 30 -> 30
    v <= 60 -> 60
    else -> ((v + 29) / 30) * 30
}
