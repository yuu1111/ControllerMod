# コントローラーMOD実装調査

## 概要

Minecraft Java Edition向けコントローラーMODの実装方法を調査し、GUI入力処理とバーチャルカーソルの実装パターンをまとめる。

## 主要なコントローラーMOD

### 1. Controllable (MrCrayfish)
- **GitHub**: https://github.com/MrCrayfish/Controllable
- **対応バージョン**: 1.20.1+ (Forge/NeoForge)
- **入力ライブラリ**: SDL2
- **特徴**:
  - バーチャルカーソル対応
  - コンテキストベースのボタンヒント
  - ラジアルホイールメニュー
  - 左右スティック個別のデッドゾーン設定

### 2. MidnightControls (TeamMidnightDust)
- **GitHub**: https://github.com/TeamMidnightDust/MidnightControls
- **対応バージョン**: 1.18+ (Fabric/NeoForge)
- **前身**: LambdaControls
- **特徴**:
  - ジョイスティック入力が1000Hz更新（FPS非依存）
  - 改善されたマウスボタンエミュレーション（EMI, REI, JEI対応）
  - タッチスクリーンサポート
  - Bedrock Edition風のUI機能

### 3. Controlify (isXander)
- **GitHub**: https://github.com/isXander/Controlify
- **対応バージョン**: 1.19.4+ (Fabric/NeoForge)
- **特徴**:
  - 全GUIをコントローラーで操作可能
  - カーソルスナッピング機能
  - データドリブン設計（リソースパックでカスタマイズ可能）
  - Legacy Console Edition モード

### 4. Joypad Mod (1.7.10対応)
- **フォーラム**: https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1283271
- **GitHub**: https://github.com/ljsimin/MinecraftJoypadSplitscreenMod
- **対応バージョン**: 1.6.4, 1.7.10, 1.8
- **特徴**:
  - アナログスティックによるバーチャルカーソル
  - スプリットスクリーン対応
  - マウスとの併用可能

---

## 実装アプローチの比較

### アプローチ1: Mixin による Mouse クラスへの注入（モダン）

**使用MOD**: MidnightControls, Controlify

```java
@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        // コントローラーモード時はマウス移動をキャンセル
        if (ControllerManager.isControllerActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        // コントローラーからのボタン入力を優先
        if (ControllerManager.isVirtualClick()) {
            // 仮想クリック処理
        }
    }
}
```

**利点**:
- 全てのGUIで統一的に動作
- MOD製GUIとの互換性が高い
- マウス位置の完全な制御が可能

**欠点**:
- Mixin依存（1.7.10では SpongePowered Mixin が必要）
- 他MODとの競合リスク

---

### アプローチ2: Screen Mixin による画面単位の注入

**使用MOD**: Controlify

```java
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        // バーチャルカーソル座標を使用
        if (VirtualMouse.isActive()) {
            double vx = VirtualMouse.getX();
            double vy = VirtualMouse.getY();
            // 座標を置換して処理
        }
    }
}
```

---

### アプローチ3: InputManager のキュー方式

**使用MOD**: MidnightControls

```java
public class InputManager {
    private static final Queue<MouseMoveEvent> mouseQueue = new ConcurrentLinkedQueue<>();

    public static void queueMoveMousePosition(float dx, float dy) {
        mouseQueue.add(new MouseMoveEvent(dx, dy));
    }

    public static void processTick() {
        while (!mouseQueue.isEmpty()) {
            MouseMoveEvent event = mouseQueue.poll();
            // GLFW経由でマウス位置を更新
            GLFW.glfwSetCursorPos(window, newX, newY);
        }
    }
}
```

---

### アプローチ4: MouseHelper 置換方式（1.7.10向け）

**使用MOD**: Joypad Mod

```java
// Minecraft.mouseHelper フィールドを置換
public class ControllerMouseHelper extends MouseHelper {

    private float virtualX, virtualY;

    @Override
    public void mouseXYChange() {
        if (isControllerMode()) {
            // コントローラー入力から仮想的なマウス移動量を計算
            this.deltaX = (int) (controllerDeltaX * sensitivity);
            this.deltaY = (int) (controllerDeltaY * sensitivity);
        } else {
            super.mouseXYChange();
        }
    }
}

// 適用方法
Minecraft.getMinecraft().mouseHelper = new ControllerMouseHelper();
```

**利点**:
- Mixin不要
- 1.7.10で動作可能
- シンプルな実装

**欠点**:
- ゲームプレイ中の視点操作のみ対応
- GUI操作には別のアプローチが必要

---

### アプローチ5: GUI固有ハンドラー方式（反射）

**現在のControllerMod実装**

```java
public class GuiSlotHandler {
    private Method elementClickedMethod;

    public void handleClick(GuiScreen screen, int x, int y) {
        GuiSlot slot = getGuiSlot(screen);
        int index = getSlotIndexAtCursor(slot, y);
        elementClickedMethod.invoke(slot, index, false, x, y);
    }
}
```

**利点**:
- Mixin不要
- GUI単位で最適化可能

**欠点**:
- GUI毎に個別実装が必要
- 新しいGUIやMOD GUIへの対応が困難

---

## MidnightControls の詳細実装

### 入力処理フロー

```
Controller Input (GLFW/SDL2)
    ↓
MidnightInput.fetchButtonStates()
    ↓
handleButton() / handleJoystick()
    ↓
[GUI開いている?]
├─ YES: changeFocus() / queueMoveMousePosition()
└─ NO: カメラ操作 / 移動操作
```

### フォーカスナビゲーション

```java
private void changeFocus(Screen screen, NavigationDirection direction) {
    if (screen instanceof SpruceScreen spruceScreen) {
        // SpruceUI用の専用ナビゲーション
        spruceScreen.onNavigation(direction, false);
    } else {
        // 標準のキーボードシミュレーション
        int key = switch (direction) {
            case UP -> GLFW_KEY_UP;
            case DOWN -> GLFW_KEY_DOWN;
            case LEFT -> GLFW_KEY_LEFT;
            case RIGHT -> GLFW_KEY_RIGHT;
        };
        InputUtil.pressKey(key);
    }
}
```

### マウスエミュレーション

```java
// ジョイスティックでマウス移動をキュー
InputManager.queueMoveMousePosition(
    this.mouseSpeedX * MidnightControlsConfig.mouseSpeed,
    this.mouseSpeedY * MidnightControlsConfig.mouseSpeed
);
```

---

## 1.7.10 での推奨実装方針

### オプション1: SpongePowered Mixin を導入

GTNHでは `SpongeMixins` が使用可能。これを利用してモダンMODと同様のアプローチが可能。

```java
// mixins.json
{
  "package": "com.github.yuu1111.controllermod.mixin",
  "mixins": [
    "GuiScreenMixin",
    "GuiSlotMixin"
  ]
}

// GuiScreenMixin.java
@Mixin(GuiScreen.class)
public class GuiScreenMixin {
    @Inject(method = "handleMouseInput", at = @At("HEAD"), cancellable = true)
    private void injectMouseInput(CallbackInfo ci) {
        if (VirtualCursor.isActive()) {
            VirtualCursor.handleMouseInput((GuiScreen)(Object)this);
            ci.cancel();
        }
    }
}
```

### オプション2: ASMによるバイトコード変換

Mixin未使用の場合、FML の `IClassTransformer` を使用。

```java
public class GuiScreenTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.gui.GuiScreen")) {
            // ASMでhandleMouseInputメソッドを変更
        }
        return basicClass;
    }
}
```

### オプション3: ハイブリッドアプローチ（推奨）

1. **基本的なGUI**: 反射 + GUI固有ハンドラー
2. **GuiSlot系**: 直接メソッド呼び出し（elementClicked）
3. **MOD GUI**: 可能な範囲で対応（対応困難なものはスキップ）

---

## 参考リソース

### GitHub リポジトリ
- [MrCrayfish/Controllable](https://github.com/MrCrayfish/Controllable)
- [TeamMidnightDust/MidnightControls](https://github.com/TeamMidnightDust/MidnightControls)
- [isXander/Controlify](https://github.com/isXander/Controlify)
- [ljsimin/MinecraftJoypadSplitscreenMod](https://github.com/ljsimin/MinecraftJoypadSplitscreenMod)

### MODページ
- [Controllable - CurseForge](https://www.curseforge.com/minecraft/mc-mods/controllable)
- [MidnightControls - Modrinth](https://modrinth.com/mod/midnightcontrols)
- [Controlify - Modrinth](https://modrinth.com/mod/controlify)

### 技術資料
- [Jabelar's GUI and Input Tutorial](http://jabelarminecraft.blogspot.com/p/minecraft-forge-1721710-gui-and-input.html)
- [FabricMC MouseMixin Example](https://github.com/FabricMC/fabric/blob/1.18.2/fabric-screen-api-v1/src/main/java/net/fabricmc/fabric/mixin/screen/MouseMixin.java)

---

## 結論

### 1.7.10 (GTNH) での最適解

1. **短期的**: GUI固有ハンドラー方式を継続（現在の実装）
2. **中期的**: SpongeMixins を導入し、`GuiScreen.handleMouseInput` に注入
3. **長期的**: MOD APIを提供し、他MOD開発者が自身のGUIに対応できるようにする

### Mixin導入のメリット

| 項目 | 反射方式 | Mixin方式 |
|------|---------|-----------|
| 汎用性 | △ GUI毎に実装 | ◎ 全GUIで動作 |
| MOD互換性 | × 個別対応必要 | ○ 自動対応 |
| 実装コスト | ○ 低い | △ 初期設定必要 |
| メンテナンス | × 高い | ○ 低い |
| 性能 | ○ 良好 | ◎ 最適 |
