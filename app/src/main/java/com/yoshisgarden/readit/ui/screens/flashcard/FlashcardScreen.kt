package com.yoshisgarden.readit.ui.screens.flashcard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yoshisgarden.readit.srs.Sm2Rating
import com.yoshisgarden.readit.ui.components.splitMeaning

@Composable
fun FlashcardScreen(
    vm: FlashcardViewModel,
    onExit: () -> Unit,
    phraseIds: List<Long> = emptyList(),
) {
    val s by vm.state.collectAsState()
    LaunchedEffect(phraseIds) { vm.load(phraseIds = phraseIds) }

    when {
        s.loading -> Centered { Text("カードを準備中…") }
        s.finished && s.queue.isEmpty() -> Centered {
            Text("今日のカードは完了！🎉", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("また明日、復習カードが届きます")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onExit, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                Text("ホームへ戻る")
            }
        }
        s.finished -> Centered {
            Text("お疲れさまでした！", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("${s.reviewedCount} 枚を復習しました 🌸")
            if (s.retryCount > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "うち ${s.retryCount} 回は「知らない」からのもう一度でした",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onExit, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                Text("ホームへ戻る")
            }
        }
        else -> CardView(s, vm)
    }
}

@Composable
private fun CardView(s: FlashcardUiState, vm: FlashcardViewModel) {
    val item = s.queue[s.index]
    val card = item.phrase

    val front: String
    val back: String
    val frontJa: Boolean
    val backJa: Boolean
    if (s.direction == CardDirection.EN_TO_JA) {
        front = card.english; back = card.japanese
        frontJa = false; backJa = true
    } else {
        front = card.japanese; back = card.english
        frontJa = true; backJa = false
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        LinearProgressIndicator(
            progress = { (s.index + 1f) / s.queue.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PhaseChip(item.phase, s.phaseLabel)
            Text(
                "${s.index + 1} / ${s.queue.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        Spacer(Modifier.height(8.dp))

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            // key(index): reset the flip animation per card so the next card's
            // answer never flashes during the flip-back transition.
            key(s.index) {
            val rotation by animateFloatAsState(
                targetValue = if (s.flipped) 180f else 0f,
                animationSpec = tween(400),
                label = "flip",
            )
            val showBack = rotation > 90f
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12 * density
                    }
                    .clickable { vm.flip() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (showBack) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (!showBack) {
                        CardFace(
                            text = front,
                            japanese = frontJa,
                            mono = s.direction == CardDirection.EN_TO_JA,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(24.dp),
                        )
                    } else {
                        CardFace(
                            text = back,
                            japanese = backJa,
                            mono = s.direction == CardDirection.JA_TO_EN,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .padding(24.dp)
                                .graphicsLayer { rotationY = 180f },
                        )
                    }
                }
            }
            }
        }

        Spacer(Modifier.height(12.dp))
        // Fixed-height area so flipping (hint <-> rate buttons) never shifts the card.
        Box(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (!s.flipped) {
                Text(
                    "カードをタップして答えを表示",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RateButton("知らない", MaterialTheme.colorScheme.error, Modifier.weight(1f)) {
                        vm.rate(Sm2Rating.UNKNOWN)
                    }
                    RateButton("うっすら", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) {
                        vm.rate(Sm2Rating.VAGUE)
                    }
                    RateButton("知ってる", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                        vm.rate(Sm2Rating.KNOWN)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** Tells the user which block of the session they are in (復習 / 新規 / もう一度). */
@Composable
private fun PhaseChip(phase: CardPhase, label: String) {
    val color = when (phase) {
        CardPhase.REVIEW -> MaterialTheme.colorScheme.tertiary
        CardPhase.RETRY -> MaterialTheme.colorScheme.error
        CardPhase.NEW -> MaterialTheme.colorScheme.primary
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.14f)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/**
 * One face of the flip card. For a Japanese face that carries a "（meaning）" note,
 * the term is shown large and the meaning appears in a soft bubble below it — so
 * katakana jargon is understandable right on the card (same idea as the dictionary).
 */
@Composable
private fun CardFace(
    text: String,
    japanese: Boolean,
    mono: Boolean,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val (term, meaning) = if (japanese) splitMeaning(text) else text to ""
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            term,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            ),
            color = color,
            textAlign = TextAlign.Center,
        )
        if (meaning.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.12f),
            ) {
                Text(
                    meaning,
                    style = MaterialTheme.typography.bodyLarge,
                    color = color,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun RateButton(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = color),
    ) { Text(label, style = MaterialTheme.typography.labelLarge) }
}

@Composable
private fun Centered(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}
