# 1.7.10 Decompile環境でのDebug実行

## 調査結果

### Minecraftソースへのブレークポイント設定
- `build/rfg/minecraft-src/java/` にデコンパイル済みソースがある
- IntelliJでブレークポイント設定可能
- Shift+F9 でデバッグ実行

### Minecraftソースの編集・反映
- `build/rfg/minecraft-src/java/` を編集可能
- **重要**: `./gradlew runClient -x decompressDecompiledSources` で実行
  - `-x decompressDecompiledSources` を付けないと編集が上書きされる
- ホットリロードは不可（再起動必要）

## EntityRenderer座標フロー調査結果

### 確認された動作 (2025-12-01)

**デバッグ出力:**
```
[EntityRenderer] skipRenderWorld=false screen=GuiMainMenu world=false
[EntityRenderer] Mouse.getX()=407 Mouse.getY()=396 -> scaled: k=203 l=41 (displaySize=854x480)
```

### 座標変換の流れ
1. **EntityRenderer.updateCameraAndRender()** (Line 1077-1078)
   ```java
   final int k = Mouse.getX() * i / this.mc.displayWidth;
   final int l = j - Mouse.getY() * j / this.mc.displayHeight - 1;
   ```

2. **スケーリング式:**
   - `k = Mouse.getX() * scaledWidth / displayWidth`
   - `l = scaledHeight - Mouse.getY() * scaledHeight / displayHeight - 1`

3. **GUIへの伝達** (Line 1143)
   ```java
   this.mc.currentScreen.drawScreen(k, l, p_78480_1_);
   ```

### 重要な発見
- **skipRenderWorld=false**: メインメニュー(world=null)でもGUIレンダリングブロックに入る
- **座標計算はEntityRendererで一度だけ行われる**
- **k, l がGuiScreen.drawScreen()のパラメータとして渡される**

## バーチャルカーソル実装方針

### オプション1: EntityRenderer.updateCameraAndRender Mixin
- `k, l` の計算後、使用前にインターセプト
- 最も上流でのインターセプト

### オプション2: GuiScreen.drawScreen Mixin (現在のアプローチ)
- drawScreenの引数をリダイレクト
- GTNHLibのMixinGuiScreenが既にこれをやっている可能性

### オプション3: Forge DrawScreenEvent.Pre
- イベントでマウス座標を取得可能だが変更は困難

## 次のアクション
1. 現在のMixinMouse実装を確認
2. EntityRendererへのMixin追加を検討
3. k, l計算直後にバーチャルカーソル座標で置換
