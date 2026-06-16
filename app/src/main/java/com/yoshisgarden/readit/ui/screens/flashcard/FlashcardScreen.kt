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

@Composable
fun FlashcardScreen(
    vm: FlashcardViewModel,
    onExit: () -> Unit,
) {
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    when {
        s.loading -> Centered { Text("カードを準備中…") }
        s.finished -> Centered {
            Text("お疲れさまでした！", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("${s.reviewedCount} 枚を復習しました 🌸")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onExit, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                Text("ホームへ戻る")
            }
        }
        else -> CardView(s, vm)
    }
}

/** Put a "（meaning）" parenthetical on its own line so long cards read clearly. */
private fun breakBeforeParen(s: String): String =
    s.replace("（", "\n（").trimStart('\n')

@Composable
private fun CardView(s: FlashcardUiState, vm: FlashcardViewModel) {
    val card = s.cards[s.index]

    val front: String
    val back: String
    if (s.direction == CardDirection.EN_TO_JA) {
        front = breakBeforeParen(card.english); back = breakBeforeParen(card.japanese)
    } else {
        front = breakBeforeParen(card.japanese); back = breakBeforeParen(card.english)
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        LinearProgressIndicator(
            progress = { (s.index + 1f) / s.cards.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${s.index + 1} / ${s.cards.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
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
                        Text(
                            front,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = if (s.direction == CardDirection.EN_TO_JA) FontFamily.Monospace else FontFamily.Default,
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp),
                        )
                    } else {
                        Text(
                            back,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = if (s.direction == CardDirection.JA_TO_EN) FontFamily.Monospace else FontFamily.Default,
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
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
