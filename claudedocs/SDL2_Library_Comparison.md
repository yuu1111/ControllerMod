# Java SDL/Controller Library 比較

## 概要

Minecraft 1.7.10 (GTNH環境) 向けコントローラーMOD実装のためのライブラリ比較

## 比較表

| 項目 | sdl2gdx | libsdl4j | libsdl4j-controlify | sdljoystick4java | LWJGL2 Controllers |
|------|---------|----------|---------------------|------------------|-------------------|
| **SDL版** | SDL2 | SDL2 | SDL3 | SDL2 | N/A (独自) |
| **バインディング方式** | JNI (gdx-jnigen) | JNA | JNA | JNI | 内蔵 |
| **最新バージョン** | 1.0.5 (2024/10) | 2.28.4-1.6 (2023/10) | 2.28.1-1.3 | 1.1.0 (2018/11) | MC同梱 |
| **Maven Central** | ✅ | ✅ | ✅ | ❌ | 不要 |
| **GTNH JNA問題** | ✅ 回避可能 | ⚠️ 影響あり | ⚠️ 影響あり | ✅ 回避可能 | ✅ 問題なし |
| **Java要件** | 8+ | 8+ | 8+ | 7+ | 6+ |

## 機能比較

| 機能 | sdl2gdx | libsdl4j | libsdl4j-controlify | sdljoystick4java | LWJGL2 |
|------|---------|----------|---------------------|------------------|--------|
| ボタン/軸 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 振動 (Rumble) | ✅ | ✅ | ✅ | ✅ | ❌ |
| ホットプラグ | ✅ | ✅ | ✅ | ✅ | ❌ |
| ジャイロ | ❌ | ✅ | ✅ | ❌ | ❌ |
| LED制御 | ✅ (XInput) | ✅ | ✅ | ❌ | ❌ |
| タッチパッド | ❌ | ✅ | ✅ | ❌ | ❌ |
| コントローラーDB | ✅ | ✅ | ✅ | ✅ | ❌ |

## プラットフォーム対応

| OS | sdl2gdx | libsdl4j | libsdl4j-controlify | sdljoystick4java | LWJGL2 |
|----|---------|----------|---------------------|------------------|--------|
| Windows x64 | ✅ | ✅ | ✅ | ✅ | ✅ |
| Windows x86 | ✅ | ✅ | ✅ | ✅ | ✅ |
| Linux x64 | ✅ | ✅ | ✅ | ❌ | ✅ |
| Linux ARM | ❌ | ✅ | ✅ | ❌ | ❌ |
| macOS | ✅ | ⚠️ 要手動 | ⚠️ 要手動 | ❌ | ✅ |
| Steam Deck | ✅ | ✅ | ✅ | ❌ | ⚠️ 基本のみ |

## 詳細分析

### 1. sdl2gdx
- **リポジトリ**: https://github.com/electronstudio/sdl2gdx
- **Maven**: `uk.co.electronstudio.sdl2gdx:sdl2gdx:1.0.+`
- **ライセンス**: GPL + Classpath Exception

**メリット**:
- JNIベースでGTNH環境のJNA問題を回避
- Maven Centralから取得可能
- 2024年も活発にメンテナンス
- 3層API (低レベル/OO/LibGDX互換)
- 振動・ホットプラグ対応

**デメリット**:
- LibGDX依存 (最小限で使用可能だが余分)
- ジャイロ/タッチパッド非対応
- macOS ARM未検証

---

### 2. libsdl4j (SDL2版)
- **リポジトリ**: https://github.com/libsdl4j/libsdl4j
- **Maven**: `io.github.libsdl4j:libsdl4j:2.28.4-1.6`
- **ライセンス**: MIT

**メリット**:
- SDL2 APIの完全マッピング
- ネイティブライブラリ同梱
- Linux ARM対応

**デメリット**:
- JNAベース → GTNHのUnsafeReflectionRedirector問題
- macOSはframeworkの手動配置必要

---

### 3. libsdl4j-controlify (SDL3版)
- **リポジトリ**: https://github.com/isXander/libsdl4j-controlify
- **Maven**: `dev.isxander.sdl3java:libsdl4j`
- **使用例**: Controlify (Minecraft 1.20+)

**メリット**:
- SDL3の最新機能 (ジャイロ、タッチパッド、高度なハプティクス)
- Minecraftモッドでの実績あり

**デメリット**:
- JNAベース → GTNH問題
- SDL3は1.7.10環境で動作未検証
- 一部ユーザーでクラッシュ報告あり

---

### 4. sdljoystick4java
- **リポジトリ**: https://github.com/jessepav/sdljoystick4java
- **配布**: GitHubリリースのみ

**メリット**:
- JNIベース (GTNH問題回避)
- Java 7対応 (1.7.10に最適)
- シンプルなAPI

**デメリット**:
- 2018年以降更新停止
- Windowsのみネイティブ提供
- Maven非公開 (手動配置必要)

---

### 5. LWJGL2 Controllers
- **提供**: Minecraft 1.7.10同梱
- **API**: `org.lwjgl.input.Controllers`

**メリット**:
- 追加依存なし
- 安定動作保証
- 即座にプロトタイプ可能

**デメリット**:
- 振動なし
- ホットプラグなし
- 基本的なボタン/軸のみ

---

## 推奨選択

### GTNH環境向け優先順位

1. **sdl2gdx** (推奨)
   - JNIベースでGTNH問題回避
   - Maven利用可能
   - 活発なメンテナンス
   - 必要十分な機能

2. **LWJGL2 Controllers** (プロトタイプ用)
   - 最速で動作確認可能
   - 依存追加不要
   - 後からsdl2gdxに移行可能

3. **sdljoystick4java** (フォールバック)
   - sdl2gdxが動かない場合の代替
   - 手動配置の手間あり

### 非推奨

- **libsdl4j系**: JNAベースでGTNH環境で問題発生リスク高

---

## 結論

**sdl2gdx** を第一候補として実装を進める

```gradle
dependencies {
    implementation "uk.co.electronstudio.sdl2gdx:sdl2gdx:1.0.+"
}
```

JNA問題が発生した場合は、LWJGL2 Controllersで基本機能を実装し、振動などは将来対応とする
