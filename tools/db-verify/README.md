# db-verify — DB マイグレーションの自動検証

`fallbackToDestructiveMigration` を使っている以上、マイグレーションを書き損じると
**ユーザーの学習データが黙って消える**。しかも消えたことに気づけない。
それを実機・エミュレータ上で毎回確かめるための道具。

逆に、画面を見れば分かる挙動（単語帳のめくり方や復習の出題順など）はここでは扱わない。
デバッグ実行で目視するほうが速く、UI 自動操作は壊れやすいだけで割に合わなかった。

前提: エミュレータか実機が1台つながっていること、`py`（Python 3）が通ること。

## 使い方

```powershell
powershell -File tools\db-verify\run_migration_check.ps1
# ビルド済みの debug APK を使い回すなら -SkipBuild
```

やっていること:

1. debug APK をインストールして `pm clear`
2. アプリに DB を作らせ、フレーズを取り込ませる
3. その DB を**ひとつ前のスキーマに巻き戻し**、SRS 進捗・学習ログ・ストリークを仕込む
4. アプリを再起動して Room に本物のマイグレーションを走らせる
5. 引き戻して「何も失われていないか」を検証

`MIGRATION VERIFIED` が出て exit 0 なら成功。

## ファイル

| ファイル | 役割 |
|---|---|
| `run_migration_check.ps1` | 一括実行 |
| `make_v1_db.py` | 取得した DB を v1 に巻き戻し、進捗を仕込む |
| `verify_migration.py` | 移行後の DB を検証（期待値はここの定数） |
| `probe_db.py` | 待ち合わせ用に `version=` `phrases=` を出力 |

## スキーマを変えたら

1. `make_v1_db.py` をコピーして `make_v2_db.py` を作り、v3 で増えたものを落とす
2. `verify_migration.py` の `EXPECTED_*` を更新（`review_logs` の列チェックも）
3. 種データを増やしたら `EXPECTED_PHRASES` と `run_migration_check.ps1` の `$phrases`

## ハマりどころ（すべて対処済み・戻さないこと）

- **`adb exec-out ... > file` は使わない。** PowerShell のリダイレクトがバイナリを壊し、
  `.db` が "file is not a database" になる。`adb pull` を使う。
- **ネイティブ exe に `2>&1` を付けない。** Windows PowerShell 5.1 では終了コード 0 でも
  ErrorRecord に包まれ、`$ErrorActionPreference = "Stop"` で落ちる。adb は進捗を stderr に出すので必ず踏む。
- **ファイルの存在で「準備完了」を判断しない。** Room は WAL モードで、シードは UI 表示より
  ずっと後に終わる。`probe_db.py` で中身を見て待つ。
- **`monkey` で起動しない。** 既存タスクに intent が配送されるだけで（`START_DELIVERED_TO_TOP`）
  Activity が再生成されず、DB が開かれない。`am start -S` を使う。
- **`pm clear` の後は通知権限を先に付与する。** さもないと権限ダイアログが最前面に居座り、
  起動が `GrantPermissionsActivity` に吸われて MainActivity が再開しない。
