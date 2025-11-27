# ControllerMod ボタンマッピング

Minecraft Bedrock Edition の標準マッピングに準拠

## 既存MOD比較

### MOD一覧

| MOD名 | 対応バージョン | 入力ライブラリ | GUI操作 | 特徴 |
|-------|--------------|--------------|--------|------|
| [Joypad Mod](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1283271-joypad-mod-usb-controller-split-screen-over-350k) | 〜1.7.10 | LWJGL/JInput | バーチャルカーソル | 分割画面対応 |
| [Controllable](https://www.curseforge.com/minecraft/mc-mods/controllable) | 1.12.2〜1.21 | SDL2 (libsdl4j) | バーチャルカーソル | Bedrock風UI、ラジアルメニュー |
| [Controlify](https://modrinth.com/mod/controlify) | 1.19.4〜1.21 | SDL2 (libsdl4j-controlify) | カーソルスナップ | 最高機能 |
| [MidnightControls](https://modrinth.com/mod/midnightcontrols) | 1.16〜1.20 | GLFW | D-Pad移動 | Fabric向け |

### GUI操作方式の比較

| 方式 | 採用MOD | 特徴 | 長所 | 短所 |
|------|--------|------|------|------|
| **バーチャルカーソル** | Joypad Mod, Controllable | スティックでマウスカーソルをエミュレート | 実装が単純、汎用性高い | 精度が必要 |
| **D-Pad移動** | MidnightControls | 十字キーでスロット間を移動 | 直感的、高速 | MOD対応が必要 |
| **カーソルスナップ** | Controlify | カーソルがスロットに吸着 | 精度不要、高速 | 実装が複雑 |
| **ラジアルメニュー** | Controllable, Controlify | 円形メニューでアクション選択 | ボタン数の節約 | 慣れが必要 |

### Controlify の機能詳細

最も高機能な [Controlify](https://github.com/isXander/Controlify) の特徴:

- **カーソルスナップ**: インベントリでカーソルがスロットに自動吸着
- **オンスクリーンキーボード**: コントローラーだけで文字入力可能
- **ラジアルメニュー**: 設定可能な円形メニュー
- **振動対応**: PC版では珍しい振動フィードバック
- **ジャイロ対応**: DualSense等のジャイロで微調整
- **フリックスティック**: ジャイロと組み合わせた高速視点操作
- **MOD API**: 他MODがGUI対応を追加可能

### Bedrock Edition の標準操作

| カテゴリ | 操作 | 方式 |
|---------|------|------|
| 移動 | 左スティック | アナログ |
| 視点 | 右スティック | アナログ |
| インベントリ | 左スティック | カーソル移動 |
| スロット選択 | A/X | 決定 |
| クイック移動 | Y | 一発転送 |
| アイテム分割 | RT長押し | 半分取る |

## ボタンマッピング (Xbox レイアウト)

### 基本操作

| ボタン | SDL2 コード | アクション | Minecraft キー |
|--------|-----------|----------|---------------|
| A | 0 | ジャンプ | Space |
| B | 1 | スニーク (ホールド) | Shift |
| X | 2 | 設定GUI | - |
| Y | 3 | インベントリ | E |

### トリガー・バンパー

| ボタン | SDL2 コード | アクション | Minecraft キー |
|--------|-----------|----------|---------------|
| RT | 軸5 | 攻撃/破壊 | 左クリック |
| LT | 軸4 | 使用/設置 | 右クリック |
| RB | 10 | 次のホットバー | マウスホイール下 |
| LB | 9 | 前のホットバー | マウスホイール上 |

### スティック

| ボタン | SDL2 コード | アクション |
|--------|-----------|----------|
| 左スティック | 軸0, 軸1 | 移動 (WASD) |
| 右スティック | 軸2, 軸3 | 視点操作 |
| L3 (左スティック押込) | 7 | ダッシュ (Ctrl) |
| R3 (右スティック押込) | 8 | 視点切替 (F5) |

### 十字キー (D-Pad)

| 方向 | SDL2 コード | アクション | Minecraft キー |
|-----|------------|----------|---------------|
| 上 | 11 | 視点切替 | F5 |
| 下 | 12 | アイテムを捨てる | Q |
| 左 | 13 | - | - |
| 右 | 14 | チャット | T |

### システム

| ボタン | SDL2 コード | アクション | Minecraft キー |
|--------|-----------|----------|---------------|
| Start | 6 | ポーズメニュー | Escape |
| Back/Select | 4 | プレイヤーリスト | Tab |

## SDL2 GameController 定数

```java
// ボタン (SDL_GameControllerButton)
// https://wiki.libsdl.org/SDL2/SDL_GameControllerButton
public static final int BUTTON_A = 0;
public static final int BUTTON_B = 1;
public static final int BUTTON_X = 2;
public static final int BUTTON_Y = 3;
public static final int BUTTON_BACK = 4;   // Select/View
public static final int BUTTON_GUIDE = 5;  // Xboxボタン
public static final int BUTTON_START = 6;  // Menu
public static final int BUTTON_L3 = 7;     // 左スティック押込
public static final int BUTTON_R3 = 8;     // 右スティック押込
public static final int BUTTON_LB = 9;     // Left Shoulder
public static final int BUTTON_RB = 10;    // Right Shoulder
public static final int BUTTON_DPAD_UP = 11;
public static final int BUTTON_DPAD_DOWN = 12;
public static final int BUTTON_DPAD_LEFT = 13;
public static final int BUTTON_DPAD_RIGHT = 14;

// 軸 (SDL_GameControllerAxis)
public static final int AXIS_LEFT_X = 0;
public static final int AXIS_LEFT_Y = 1;
public static final int AXIS_RIGHT_X = 2;
public static final int AXIS_RIGHT_Y = 3;
public static final int AXIS_TRIGGER_LEFT = 4;
public static final int AXIS_TRIGGER_RIGHT = 5;
```

## 実装状況

### Phase 1 (必須) ✅
- [x] 左スティック: 移動
- [x] 右スティック: 視点操作
- [x] A: ジャンプ
- [x] RT: 攻撃
- [x] LT: 使用/設置

### Phase 2 (基本) ✅
- [x] B: スニーク
- [x] Y: インベントリ
- [x] RB/LB: ホットバー切り替え
- [x] Start: ポーズ

### Phase 3 (拡張) ✅
- [x] L3: ダッシュ
- [x] R3: 視点切替
- [x] 十字キー: 各種機能 (上=視点切替, 下=ドロップ, 右=チャット)
- [x] Back: プレイヤーリスト

### Phase 4 (設定GUI) ✅
- [x] X: 設定GUI (GTNHLib SimpleGuiConfig)
- [x] デッドゾーン設定 (ControllerConfig.deadzone)
- [x] 感度設定 (ControllerConfig.lookSensitivity)
- [x] Y軸反転設定 (ControllerConfig.invertY)
- [x] トリガー閾値設定 (ControllerConfig.triggerThreshold)
- [x] 設定の永続化 (GTNHLib @Config)

## ControllerMod 実装方針

### 入力ライブラリ
- **sdl2gdx** (JNI経由SDL2)
- GTNHのJNA問題を回避
- Steam Deck完全対応

### GUI操作方針 (検討中)

| Phase | 方式 | 理由 |
|-------|------|------|
| Phase 1 | なし (ゲーム中のみ) | 基本機能を優先 |
| Phase 4 | バーチャルカーソル | 実装が単純、MOD互換性 |
| 将来 | カーソルスナップ | UX向上 |

### 1.7.10 特有の考慮事項
- GuiScreenのスロット構造が古い
- MOD GUIが多数存在 (NEI, AE2等)
- バーチャルカーソルが最も安全

## 参考リンク

- [Minecraft Controls (公式)](https://www.minecraft.net/en-us/article/minecraft-controls)
- [Minecraft Wiki - Controller Tutorial](https://minecraft.wiki/w/Tutorial:Playing_with_a_controller)
- [SDL2 GameController API](https://wiki.libsdl.org/SDL2/CategoryGameController)
- [Controlify GitHub](https://github.com/isXander/Controlify)
- [Controllable GitHub](https://github.com/MrCrayfish/Controllable)
