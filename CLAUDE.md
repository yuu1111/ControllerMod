# ControllerMod

## Claude向けルール

- **対話言語**: 日本語で対話すること
- **コードコメント**: 日本語で書くこと

## プロジェクト概要

Minecraft 1.7.10向けのコントローラー入力MOD
Steam Deck対応を視野に入れ、SDL2ベースで実装

## 技術スタック

- **Minecraft**: 1.7.10
- **ビルドシステム**: GTNH ForgeGradle (ExampleMod1.7.10テンプレート)
- **Java**: 8 (Jabel経由でJava 17構文使用可能)
- **コントローラーライブラリ**: sdl2gdx (JNI経由)

## パッケージ構成

```
com.github.yuu1111.controllermod
├── ControllerMod.java           # メインModクラス
├── ClientProxy.java             # クライアント側処理 (コントローラー初期化)
├── CommonProxy.java             # 共通処理
└── controller/
    └── ControllerHandler.java   # SDL2コントローラー管理
```

## 現在の実装状態

### 完了
- [x] sdl2gdx統合 (JitPack経由)
- [x] コントローラー検出
- [x] 接続/切断ログ出力
- [x] ボタン/軸入力のログ出力

### 動作確認済み
- Xbox One Elite Controller (XInput)

## SDL2実装

### 使用ライブラリ

**[sdl2gdx](https://github.com/electronstudio/sdl2gdx)** (v1.0.5)
- JNIベース (gdx-jnigen) → GTNH JNA問題を回避
- JitPack経由で取得 (JCenterは終了済み)
- 振動・ホットプラグ対応
- LibGDX Controller API互換

```gradle
// repositories.gradle
maven { url "https://jitpack.io" }

// dependencies.gradle
implementation "com.github.electronstudio:sdl2gdx:1.0.5"
```

### なぜSDL2か

| 機能 | SDL2 | GLFW |
|------|------|------|
| ボタン/軸 | ✅ | ✅ |
| 振動 | ✅ | ❌ |
| LED制御 | ✅ | ❌ |
| ジャイロ | ❌ (sdl2gdx) | ❌ |
| Steam Deck | ✅ 最適 | △ |

### JNA回避の理由

GTNHのlwjgl3ifyには`UnsafeReflectionRedirector`があり、JNA経由でのSDL2呼び出しが失敗する
JNIベースのsdl2gdxを使用することでこの問題を回避

## 次のステップ

1. **入力ハンドラー実装**
   - ボタン→キーバインドマッピング
   - スティック→移動/視点操作
   - トリガー→攻撃/使用

2. **GUI操作対応**
   - カーソル移動
   - インベントリ操作
   - メニュー操作

3. **設定画面**
   - キーマッピングカスタマイズ
   - デッドゾーン設定
   - 感度設定

## ビルドコマンド

```bash
# ビルド
./gradlew build

# クライアント起動
./gradlew runClient

# フォーマット適用
./gradlew spotlessApply
```

## 関連リソース

- [GTNH ExampleMod](https://github.com/GTNewHorizons/ExampleMod1.7.10)
- [sdl2gdx](https://github.com/electronstudio/sdl2gdx)
- [SDL2 GameController API](https://wiki.libsdl.org/SDL2/CategoryGameController)
- [ライブラリ比較](claudedocs/SDL2_Library_Comparison.md)
