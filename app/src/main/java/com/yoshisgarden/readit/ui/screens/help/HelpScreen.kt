package com.yoshisgarden.readit.ui.screens.help

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
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
            SectionTitle("使い方")
            HelpItem("📖 辞書", "IT英語のフレーズを検索・カテゴリ別に閲覧できます。カタカナ語など意味つきの単語は背景色が変わり、タップすると吹き出しで意味が出ます。")
            HelpItem("🗂 単語帳", "「めくり」はカードを裏返して「知らない／知ってる」で答える形式、「3択」は意味を3つから選ぶ形式です（画面上部で切り替え、次回も同じ形式で始まります）。3択では1回目で正解すれば「知ってる」、間違えれば「知らない」の扱いです。「知らない」になったカードは同じセッション中に数枚あとでもう一度出て、次回は前回の取りこぼしの復習から始まります。")
            HelpItem("❓ クイズ", "穴埋め・エラー解析・読解の3モード。選択式でIT英語の理解を確認できます。")
            HelpItem("📊 進捗グラフ", "毎日の学習時間と目標達成状況、連続日数を確認できます。目標分は設定で変更できます。")

            Spacer(Modifier.height(20.dp))
            SectionTitle("ワンポイント")
            HelpItem("💬 意味の吹き出し", "辞書・単語帳で、カタカナ語や略語には意味が添えてあります。分からない用語が出たらタップ／カード裏で確認しましょう。")
            HelpItem("🔥 連続日数とフェーズ", "毎日少しでも学習すると連続日数が伸びます。学習を重ねるとフェーズが上がり、読める英語の範囲が広がります。")

            Spacer(Modifier.height(24.dp))
            Text(
                "バージョン情報・開発支援は、メニューの「バージョン情報」にあります。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
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
