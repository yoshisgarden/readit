package com.yoshisgarden.readit.ui.screens.weak

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yoshisgarden.readit.data.WeakPhrase
import com.yoshisgarden.readit.ui.components.splitMeaning

/**
 * Phrases the user has answered 知らない / うっすら at least once, most-missed first,
 * with a shortcut to run a flashcard session over the worst offenders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeakPhrasesScreen(
    vm: WeakPhrasesViewModel,
    onBack: () -> Unit,
    onReview: (List<Long>) -> Unit,
    onOpenPhrase: (Long) -> Unit,
) {
    val phrases by vm.phrases.collectAsState()
    val batch = phrases.take(WeakPhrasesViewModel.REVIEW_BATCH).map { it.phrase.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("苦手フレーズ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
            )
        },
    ) { padding ->
        if (phrases.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "まだ苦手フレーズはありません。\n単語帳で「知らない」を選んだフレーズがここに集まります。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(
                "間違えた回数の多い順です",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 4.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(phrases, key = { it.phrase.id }) { w ->
                    WeakRow(w, onClick = { onOpenPhrase(w.phrase.id) })
                }
            }
            Button(
                onClick = { onReview(batch) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) { Text("この ${batch.size} 件を復習する") }
        }
    }
}

@Composable
private fun WeakRow(w: WeakPhrase, onClick: () -> Unit) {
    val (term, _) = splitMeaning(w.phrase.japanese)
    Card(onClick = onClick, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    w.phrase.english,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    term,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(0.dp))
            Tally("✗", w.unknownCount + w.vagueCount, MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(0.dp))
            Tally("○", w.knownCount, MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun Tally(mark: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.padding(start = 6.dp),
    ) {
        Text(
            "$mark $count",
            style = MaterialTheme.typography.labelLarge,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
