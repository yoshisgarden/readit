package com.yoshisgarden.readit.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup

/** Phase progress ring (Compose Canvas, per design doc 3.2.1). */
@Composable
fun ProgressRing(
    progress: Float,
    label: String,
    sublabel: String,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "ring",
    )
    val track = MaterialTheme.colorScheme.surfaceVariant
    val fg = MaterialTheme.colorScheme.primary
    val fg2 = MaterialTheme.colorScheme.secondary
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(160.dp)) {
        Canvas(Modifier.size(160.dp)) {
            val stroke = 18.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = fg,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.titleLarge, color = fg)
            Text(sublabel, style = MaterialTheme.typography.bodyMedium, color = fg2)
        }
    }
}

@Composable
fun CategoryFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.padding(end = 8.dp),
    )
}

@Composable
fun PhraseCard(
    english: String,
    japanese: String,
    category: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    english,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                JapaneseMeaning(japanese, MaterialTheme.typography.bodyMedium)
                Text(
                    category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "お気に入り",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
fun StatPill(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/** Returns the term part of "用語（意味）", or the whole string if no parenthetical. */
fun termOnly(japanese: String): String {
    val i = japanese.indexOf('（')
    return if (i >= 0) japanese.substring(0, i).trim() else japanese
}

/**
 * Japanese text that may carry a "（meaning）" note. When it does, only the term is
 * shown (with a subtle highlight) and tapping it toggles a tooltip with the meaning.
 * Tapping again — or tapping outside — hides it.
 */
@Composable
fun JapaneseMeaning(
    text: String,
    style: TextStyle,
    color: Color = Color.Unspecified,
) {
    val idx = text.indexOf('（')
    if (idx < 0) {
        Text(text, style = style, color = color)
        return
    }
    val term = text.substring(0, idx)
    val meaning = text.substring(idx).trim('（', '）')
    var show by remember(text) { mutableStateOf(false) }

    // Inline reveal (no floating Popup, so it never intercepts other taps such as
    // the bottom navigation). Tap the term to toggle the meaning bubble.
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                .clickable { show = !show }
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(term, style = style, color = color)
            Spacer(Modifier.size(4.dp))
            Icon(
                Icons.Outlined.Info,
                contentDescription = "意味を表示",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        }
        if (show) {
            Spacer(Modifier.size(4.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 4.dp,
            ) {
                Text(
                    meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}
