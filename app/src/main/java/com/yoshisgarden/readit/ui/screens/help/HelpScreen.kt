package com.yoshisgarden.readit.ui.screens.help

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.yoshisgarden.readit.BuildConfig

private const val STRIPE_URL = "https://buy.stripe.com/6oU7sEgo3ehH01F2kO8g002"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ヘルプ") },
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
            // ---- How to use ----
            SectionTitle("使い方")
            HelpItem("📖 辞書", "IT英語のフレーズを検索・カテゴリ別に閲覧できます。カタカナ語など意味つきの単語は背景色が変わり、タップすると吹き出しで意味が出ます。")
            HelpItem("🗂 単語帳", "カードをタップして裏返し、英語⇔日本語を覚えます。「知らない／うっすら／知ってる」で答えると、忘れたころに再出題されます（間隔反復）。")
            HelpItem("❓ クイズ", "穴埋め・エラー解析・読解の3モード。選択式でIT英語の理解を確認できます。")
            HelpItem("📊 進捗グラフ", "毎日の学習時間と目標達成状況、連続日数を確認できます。目標分は設定で変更できます。")

            Spacer(Modifier.height(20.dp))
            SectionTitle("ワンポイント")
            HelpItem("💬 意味の吹き出し", "辞書・単語帳で、カタカナ語や略語には意味が添えてあります。分からない用語が出たらタップ／カード裏で確認しましょう。")

            Spacer(Modifier.height(24.dp))
            // ---- Version & support ----
            SectionTitle("バージョン情報")
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ),
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("ReadIT", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "バージョン ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "開発: Yoshi's Garden",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "ReadIT は個人が無料・広告なしで開発・配布しています。応援いただける方は下記から支援できます（金額は自由・完全に任意で、アプリの機能には一切影響しません）。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = { context.openUrl(STRIPE_URL) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Favorite, null)
                        Spacer(Modifier.size(8.dp))
                        Text("☕ 開発を支援する（Stripe）")
                    }
                    Text(
                        "カード / Google Pay 対応",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "ReadIT v${BuildConfig.VERSION_NAME}  ·  Yoshi's Garden",
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
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun HelpItem(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(2.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun android.content.Context.openUrl(url: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
