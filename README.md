# ReadIT（リードイット）

> Claude Code・GitHub・Anthropic ドキュメントなど、日常的に触れる **IT 英語を翻訳なしで読み解ける** ようになるための Android 学習アプリ。

単語帳・進捗管理・練習テスト・フレーズ辞書の 4 機能を 1 つに統合した、すき間時間学習アプリです。会話ではなく「**読む**」ことに特化しています。

## 主な機能

| 機能 | 概要 |
|---|---|
| 📖 フレーズ辞書 | IT 英語フレーズ集。カテゴリ（Claude Code / GitHub / API Docs / Error Messages / PR Review）・全文検索・お気に入り・例文＆解説付き |
| 🗂 単語帳（SRS） | SM-2 間隔反復アルゴリズムで効率記憶。英→日 / 日→英 のフリップカード、3 段階評価 |
| ✏️ 練習テスト | 穴埋め・エラー解析・読解の 3 モード。正答率を記録 |
| 📊 進捗管理 | フェーズ進捗リング・連続学習日数（ストリーク）・学習時間グラフ |

## デザイン / テーマ

4 つのテーマカラーから選べます（設定画面でいつでも変更可）。

- 🌸 **さくらパステル** — やさしいピンク × ラベンダー（かわいい系）
- 🌿 **ミントパステル** — さわやかなミント × アクア（かわいい系）
- 🖤 **シック** — 落ち着いたスレート × ベージュ（かっこいい系）
- 💎 **クール** — インディゴ × シアン（かっこいい系）

ダークモード対応、Android 12+ ではダイナミックカラー（壁紙連動）にも対応。

## 技術スタック

- Kotlin + Jetpack Compose（Material 3）
- MVVM + Repository パターン（ViewModel + StateFlow）
- Room（ローカル DB・完全オフライン動作）
- Navigation Compose / DataStore / kotlinx.serialization
- 最小 SDK 31（Android 12）/ ターゲット SDK 35

## ビルド

```bash
./gradlew :app:assembleRelease
```

リリース署名は `keystore.properties`（`keystore.properties.example` を参照）で設定します。
未設定の場合はデバッグ署名でビルドされます。

## フレーズデータについて

初期フレーズは `app/src/main/assets/phrases_v1.json` に同梱しています（v1.0 時点で 75 フレーズ）。
設計上は 500 フレーズを目標としており、同 JSON に追記するだけで拡張できます（`id` を一意にすれば次回起動時に取り込まれます）。

## 開発支援（ドネーション）

ReadIT は個人が無料・広告なしで開発・配布しています。
応援いただける方は **GitHub Sponsors** から支援いただけます（任意・機能には影響しません）。

→ https://github.com/sponsors/yoshisgarden

## ライセンス

MIT License © Yoshi's Garden
