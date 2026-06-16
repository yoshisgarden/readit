package com.yoshisgarden.readit.ui.screens.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun QuizScreen(
    modeId: String,
    vm: QuizViewModel,
    onExit: () -> Unit,
) {
    val s by vm.state.collectAsState()

    LaunchedEffect(modeId) {
        val mode = QuizMode.entries.firstOrNull { it.id == modeId } ?: QuizMode.FILL_BLANK
        vm.start(mode)
    }

    when {
        s.questions.isEmpty() && !s.finished -> {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { Text("問題を準備中…") }
        }
        s.finished -> QuizResultView(s, onRetry = { vm.start(s.mode) }, onExit = {
            vm.reset(); onExit()
        })
        else -> QuestionView(s, onSelect = vm::select, onNext = vm::next)
    }
}

@Composable
private fun QuestionView(
    s: QuizUiState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val q = s.questions[s.index]
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        LinearProgressIndicator(
            progress = { (s.index + 1f) / s.questions.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${s.index + 1} / ${s.questions.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(16.dp))

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Text(
                q.prompt,
                modifier = Modifier.padding(18.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = if (q.promptMono) FontFamily.Monospace else FontFamily.Default,
                ),
            )
        }
        Spacer(Modifier.height(20.dp))

        q.options.forEachIndexed { i, opt ->
            val container = when {
                !s.answered -> MaterialTheme.colorScheme.surface
                i == q.correctIndex -> MaterialTheme.colorScheme.primaryContainer
                i == s.selected -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
            OutlinedButton(
                onClick = { onSelect(i) },
                enabled = !s.answered,
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = container,
                ),
            ) {
                Text(
                    opt,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        if (s.answered) {
            Spacer(Modifier.height(12.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            ) {
                Text(
                    q.explanation,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (s.index + 1 >= s.questions.size) "結果を見る" else "次へ")
            }
        }
    }
}

@Composable
private fun QuizResultView(
    s: QuizUiState,
    onRetry: () -> Unit,
    onExit: () -> Unit,
) {
    val pct = if (s.questions.isEmpty()) 0 else s.correctCount * 100 / s.questions.size
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("結果", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "$pct%",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("${s.correctCount} / ${s.questions.size} 正解")
        Spacer(Modifier.height(8.dp))
        Text(
            when {
                pct >= 80 -> "すばらしい！この調子です 🎉"
                pct >= 50 -> "いい感じ！弱点を復習しましょう"
                else -> "辞書で見直してまた挑戦しよう"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("もう一度") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("モード選択へ戻る") }
    }
}
