# GTNH Mixin 実装ガイド

## 1. Mixin ローダーの選択肢

### 歴史的経緯

1.7.10では複数のMixinローダーが乱立し、互換性問題が発生していた。
現在は **UniMixins** が統一ソリューションとして推奨されている。

### 比較表

| ローダー | 状態 | 特徴 |
|---------|------|------|
| **UniMixins** | ✅ 推奨 | 全ローダーを統合、最大互換性 |
| **GTNHMixins** | ⚠️ 非推奨 | UniMixinsへ移行中 |
| **SpongeMixins** | ⚠️ レガシー | UniMixinsでエミュレート |
| **MixinBooterLegacy** | ⚠️ レガシー | UniMixinsでエミュレート |

---

## 2. UniMixins の構成

[UniMixins GitHub](https://github.com/LegacyModdingMC/UniMixins)

### モジュール構成

| モジュール | 対応バージョン | 機能 |
|-----------|--------------|------|
| **Mixin (UniMix)** | 1.7.10~1.12.2 | Fabricフォークベースのコアmixin |
| **SpongeMixins** | 1.7.10のみ | SpongeMixins互換 |
| **GTNHMixins** | 1.7.10のみ | GTNHMixins互換 + MixinExtras |
| **MixinBooterLegacy** | 1.7.10のみ | MixinBooterLegacy互換 |
| **Mixingasm** | 1.7.10のみ | ASMトランスフォーマー互換性向上 |
| **MixinExtras** | 全バージョン | 追加のMixin機能 |

---

## 3. GTNHプロジェクトでのMixin設定

### 3.1 gradle.properties の設定

```properties
# Mixin有効化
usesMixins = true

# Mixinパッケージ（必須）
# このパッケージ外にMixinを置くとビルドエラー
mixinsPackage = mixins

# デバッグ用（開発時のみ）
usesMixinDebug = true

# 別ソースセットでコンパイル（ビルド高速化）
separateMixinSourceSet =

# カスタムMixinプラグイン（通常は空）
mixinPlugin =

# CoreModクラス（必要な場合）
coreModClass =
```

### 3.2 自動生成される設定

`usesMixins = true` を設定すると:

1. **UniMixins** が自動的に依存関係に追加
2. **mixins.modid.json** が自動生成（リソースフォルダ直下）
3. 開発環境のVM引数が自動設定

---

## 4. Mixin設定ファイル

### 4.1 基本構成: mixins.modid.json

```json
{
  "required": true,
  "minVersion": "0.8.5-GTNH",
  "package": "com.github.yuu1111.controllermod.mixins",
  "refmap": "mixins.controllermod.refmap.json",
  "target": "@env(DEFAULT)",
  "compatibilityLevel": "JAVA_8",
  "mixins": [
    "MixinGuiScreen"
  ],
  "client": [
    "client.MixinMouse"
  ]
}
```

### 4.2 Early/Late Mixin（GTNHMixins機能）

**Early Mixin**: Minecraft, Forge, CoreModをターゲット
```json
// mixins.controllermod.early.json
{
  "package": "com.github.yuu1111.controllermod.mixins.early",
  "mixins": ["MixinMinecraft"]
}
```

**Late Mixin**: 他のMODをターゲット
```json
// mixins.controllermod.late.json
{
  "package": "com.github.yuu1111.controllermod.mixins.late",
  "mixins": ["MixinSomeMod"]
}
```

---

## 5. Mixinクラスの実装例

### 5.1 GuiScreen.handleMouseInput への注入

```java
package com.github.yuu1111.controllermod.mixins;

import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.yuu1111.controllermod.gui.VirtualCursor;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

    /**
     * handleMouseInput の先頭に注入
     * バーチャルカーソルがアクティブな場合、標準のマウス処理をスキップ
     */
    @Inject(method = "handleMouseInput", at = @At("HEAD"), cancellable = true)
    private void onHandleMouseInput(CallbackInfo ci) {
        if (VirtualCursor.isActiveStatic()) {
            // バーチャルカーソルで処理
            VirtualCursor.handleMouseInputStatic((GuiScreen)(Object)this);
            ci.cancel();
        }
    }
}
```

### 5.2 Mouse.getX/getY のリダイレクト

```java
package com.github.yuu1111.controllermod.mixins;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.yuu1111.controllermod.gui.VirtualCursor;

@Mixin(value = Mouse.class, remap = false)
public class MixinMouse {

    /**
     * Mouse.getX() を仮想座標で置換
     */
    @Inject(method = "getX", at = @At("HEAD"), cancellable = true)
    private static void onGetX(CallbackInfoReturnable<Integer> cir) {
        if (VirtualCursor.isActiveStatic()) {
            cir.setReturnValue(VirtualCursor.getDisplayX());
        }
    }

    /**
     * Mouse.getY() を仮想座標で置換
     */
    @Inject(method = "getY", at = @At("HEAD"), cancellable = true)
    private static void onGetY(CallbackInfoReturnable<Integer> cir) {
        if (VirtualCursor.isActiveStatic()) {
            cir.setReturnValue(VirtualCursor.getDisplayY());
        }
    }
}
```

### 5.3 マウスボタン状態の注入

```java
@Mixin(value = Mouse.class, remap = false)
public class MixinMouseButton {

    /**
     * Mouse.isButtonDown() を仮想ボタン状態で置換
     */
    @Inject(method = "isButtonDown", at = @At("HEAD"), cancellable = true)
    private static void onIsButtonDown(int button, CallbackInfoReturnable<Boolean> cir) {
        if (VirtualCursor.isActiveStatic()) {
            cir.setReturnValue(VirtualCursor.isMouseButtonDown(button));
        }
    }
}
```

---

## 6. ディレクトリ構成

```
src/main/
├── java/
│   └── com/github/yuu1111/controllermod/
│       ├── ControllerMod.java
│       ├── gui/
│       │   └── VirtualCursor.java
│       └── mixins/           # Mixinパッケージ
│           ├── MixinGuiScreen.java
│           └── MixinMouse.java
└── resources/
    └── mixins.controllermod.json
```

---

## 7. 開発環境での実行

### VM引数（自動設定される）

```
--tweakClass org.spongepowered.asm.launch.MixinTweaker
--mixin mixins.controllermod.json
```

### デバッグ引数（usesMixinDebug = true）

```
-Dmixin.debug.verbose=true
-Dmixin.debug.export=true
```

`-Dmixin.debug.export=true` を有効にすると、変換後のクラスが
`.minecraft/run/.mixin.out/` に出力される。

---

## 8. 注意事項

### 8.1 remap について

- **Minecraftクラス**: `remap = true`（デフォルト）
- **LWJGLクラス（Mouse等）**: `remap = false` が必要

```java
// LWJGLのMouseクラスは難読化されないため
@Mixin(value = Mouse.class, remap = false)
```

### 8.2 互換性

- **他のMixin MODとの競合**: 同じメソッドに複数のMixinが注入される場合、優先度設定が必要
- **ASMトランスフォーマー**: Mixingasmモジュールで互換性向上

### 8.3 パフォーマンス

- Mixin注入はゲーム起動時に一度だけ実行
- ランタイムのオーバーヘッドは最小限

---

## 9. 参考リソース

### GitHub
- [UniMixins](https://github.com/LegacyModdingMC/UniMixins)
- [GTNHMixins](https://github.com/GTNewHorizons/GTNHMixins)
- [Hodgepodge（実装例）](https://github.com/GTNewHorizons/Hodgepodge)
- [ExampleMod1.7.10](https://github.com/GTNewHorizons/ExampleMod1.7.10)

### ドキュメント
- [SpongePowered Mixin Wiki](https://github.com/SpongePowered/Mixin/wiki)
- [Mixin Javadoc](https://jenkins.liteloader.com/view/Other/job/Mixin/javadoc/)

### MODページ
- [UniMixins - Modrinth](https://modrinth.com/mod/unimixins)
- [GTNHMixins - CurseForge](https://www.curseforge.com/minecraft/mc-mods/gtnhmixins)
