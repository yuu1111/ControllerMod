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
├── config/
│   └── ControllerConfig.java    # GTNHLib @Config設定クラス
├── controller/
│   ├── ControllerHandler.java   # SDL2コントローラー管理
│   └── InputHandler.java        # ボタン→アクションマッピング
└── gui/
    └── GuiFactory.java          # GTNHLib SimpleGuiFactory
```

## 現在の実装状態

### Phase 1-3: 基本操作 ✅
- [x] 左スティック: 移動 (WASD)
- [x] 右スティック: 視点操作
- [x] A/B/Y: ジャンプ/スニーク/インベントリ
- [x] RT/LT: 攻撃/使用
- [x] RB/LB: ホットバー切り替え
- [x] Start: ポーズ
- [x] D-Pad: 各種機能

### Phase 4: 設定GUI ✅
- [x] X: 設定GUI (GTNHLib SimpleGuiConfig)
- [x] デッドゾーン設定
- [x] 感度設定
- [x] Y軸反転設定
- [x] 設定の永続化 (@Config)

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

1. **Phase 5: GUI操作対応**
   - バーチャルカーソル (左スティック)
   - インベントリ操作 (A=決定, B=キャンセル)
   - D-Padナビゲーション

2. **将来の拡張**
   - ボタンマッピングカスタマイズ
   - 複数コントローラー対応
   - 振動フィードバック

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
- [GTNHLib](https://github.com/GTNewHorizons/GTNHLib) - Config GUI自動生成
- [sdl2gdx](https://github.com/electronstudio/sdl2gdx)
- [SDL2 GameController API](https://wiki.libsdl.org/SDL2/CategoryGameController)
- [ボタンマッピング詳細](claudedocs/ControllerMapping.md)
