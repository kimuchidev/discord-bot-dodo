# copilot-instructions.md

## 概要
このプロジェクトは、FFXIV向けDiscordボット「discord-bot-dodo」です。主にパーティ募集や伝言、読み上げなどの機能を提供します。

## プロンプトコードの特徴
- SlashCommandListener/MessageCommandListenerを継承したコマンドリスナーで、Discordのスラッシュコマンドやメッセージコマンドを処理します。
- パーティ募集（PartySlashCommandListener）や伝言（ReplyAgentCommandListener）など、用途ごとにリスナーを分離。
- 募集ロールや日時、コメントなどをフォーム入力で受け取り、エンティティ（Party等）に変換して管理します。
- 募集掲示板の自動編成や、リアクションによる参加管理、メッセージの自動クリーンアップ等のロジックを含みます。
- voicevox等の外部APIを利用した読み上げ機能も実装されています。

## コーディング方針
- Spring Boot + JDA（Java Discord API）を利用。
- 各種コマンドやイベントはListenerクラスで分離・管理。
- エンティティやDTOはLombokで簡潔に記述。
- 設定値はapplication.yamlや環境変数で管理。
- テストはJUnitで自動編成ロジック等をカバー。

## 命名規則・設計
- コマンド名やロール名はFFXIVの用語に準拠。
- メソッド名・変数名は日本語コメントで補足。
- 例外処理やバリデーションはユーザー向けメッセージを重視。

## 注意事項
- DiscordのトークンやAPIキー等の機密情報は環境変数で管理し、ソースコードに含めないこと。
- 外部APIの利用制限やエラー時のリトライ処理に注意。
- パーティ募集の自動編成ロジックはテストで十分に検証すること。
