package com.yoshisgarden.readit.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.yoshisgarden.readit.ui.components.ProgressRing
import com.yoshisgarden.readit.ui.components.StatPill

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenFlashcards: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenDictionary: () -> Unit,
    onOpenPhrase: (Long) -> Unit,
    onOpenProgress: () -> Unit,
    onOpenWeak: () -> Unit,
) {
    val s by vm.state.collectAsState()

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            ProgressRing(
                progress = s.phaseProgress,
                label = s.phaseLabel,
                sublabel = "${(s.phaseProgress * 100).toInt()}%",
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            s.phaseGoal,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatPill(
                value = "🔥 ${s.progress.streakDays}",
                label = "連続日数",
                modifier = Modifier.weight(1f),
            )
            StatPill(
                value = "${s.progress.totalPhrases}",
                label = "のべ学習量",
                modifier = Modifier.weight(1f),
            )
            StatPill(
                value = "${s.todayLog?.durationMin ?: 0}分",
                label = "今日の学習",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Today's review call-to-action
        Card(
            onClick = onOpenFlashcards,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Style, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(Modifier.height(0.dp))
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(
                        if (s.dueCount > 0) "今日の学習: ${s.dueCount} 枚" else "今日のカードは完了！🎉",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        if (s.dueCount > 0) "復習＋新しいカードをめくって記憶を定着させよう"
                        else "また明日、復習カードが届きます",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("メニュー", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeAction("辞書", Icons.AutoMirrored.Filled.MenuBook, Modifier.weight(1f), onOpenDictionary)
            HomeAction("クイズ", Icons.Filled.Quiz, Modifier.weight(1f), onOpenQuiz)
            HomeAction("苦手", Icons.Filled.ErrorOutline, Modifier.weight(1f), onOpenWeak)
            HomeAction("進捗", Icons.Filled.LocalFireDepartment, Modifier.weight(1f), onOpenProgress)
        }

        Spacer(Modifier.height(20.dp))

        // Phrase of the day
        s.phraseOfDay?.let { p ->
            Text("今日のフレーズ", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Card(
                onClick = { onOpenPhrase(p.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        p.english,
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(p.japanese, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        p.exampleEn,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
private fun HomeAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
