package com.github.yuu1111.controllermod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.input.Mouse;

import com.github.yuu1111.controllermod.ControllerMod;

/**
 * バーチャルカーソルの静的マネージャー
 *
 * <p>
 * Mixinから呼び出し可能な静的メソッドを提供する
 * 内部でVirtualCursorインスタンスを管理し、
 * 座標変換やマウス入力のエミュレーションを行う
 *
 * <p>
 * 入力デバイスの自動切り替え機能:
 * <ul>
 * <li>マウスが動いた → マウスモード (バーチャルカーソル非表示)</li>
 * <li>コントローラー入力 → コントローラーモード (バーチャルカーソル表示)</li>
 * </ul>
 *
 * @author yuu1111
 */
public final class VirtualCursorManager {

    /** シングルトンインスタンス */
    private static VirtualCursor instance;

    /** バーチャルカーソルモードが有効か */
    private static boolean enabled = true;

    /** コントローラー入力がアクティブか (スティック/ボタン使用中) */
    private static boolean controllerInputActive = false;

    /** デバッグログのスロットリング用カウンター */
    private static int debugCounter = 0;

    private VirtualCursorManager() {
        // ユーティリティクラス
    }

    /**
     * VirtualCursorインスタンスを取得する
     *
     * @return VirtualCursorインスタンス
     */
    public static VirtualCursor getInstance() {
        if (instance == null) {
            instance = new VirtualCursor();
            ControllerMod.LOG.info("VirtualCursorManager: Instance created");
        }
        return instance;
    }

    /**
     * VirtualCursorインスタンスを設定する
     *
     * <p>
     * 主にControllerManagerから呼び出される
     *
     * @param cursor VirtualCursorインスタンス
     */
    public static void setInstance(VirtualCursor cursor) {
        instance = cursor;
    }

    /**
     * バーチャルカーソルが有効かどうかを返す
     *
     * <p>
     * Mixinから呼び出される 以下の条件が全て満たされた場合にtrueを返す:
     * <ul>
     * <li>バーチャルカーソルモードが有効</li>
     * <li>VirtualCursorインスタンスが存在</li>
     * <li>VirtualCursorがアクティブ状態</li>
     * </ul>
     *
     * @return バーチャルカーソルが有効な場合は {@code true}
     */
    public static boolean isActive() {
        if (!enabled || instance == null) {
            return false;
        }
        return instance.isActive();
    }

    /**
     * バーチャルカーソルモードの有効/無効を設定する
     *
     * @param value 有効にする場合は {@code true}
     */
    public static void setEnabled(boolean value) {
        enabled = value;
    }

    /**
     * バーチャルカーソルモードが有効かどうかを返す
     *
     * @return 有効な場合は {@code true}
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * コントローラー入力がアクティブかどうかを返す
     *
     * <p>
     * Mixinから呼び出される コントローラーのスティック/ボタンが
     * 使用されている間はtrueを返す これにより実マウスとの共存が可能
     *
     * @return コントローラー入力がアクティブな場合は {@code true}
     */
    public static boolean isControllerInputActive() {
        return controllerInputActive && isActive();
    }

    /**
     * コントローラー入力のアクティブ状態を設定する
     *
     * <p>
     * VirtualCursorから呼び出される スティック入力やボタン押下時にtrueを設定
     *
     * @param active アクティブにする場合は {@code true}
     */
    public static void setControllerInputActive(boolean active) {
        if (active && !controllerInputActive) {
            // コントローラーモードに切り替え
            if (debugCounter++ % 60 == 0) {
                ControllerMod.LOG.info("Input mode: Controller");
            }
        }
        controllerInputActive = active;
    }

    /**
     * マウスクリックを検出してモードを切り替える
     *
     * <p>
     * 毎フレーム呼び出されることを想定
     * Minecraftウィンドウがフォーカスされている状態で
     * 実マウスのボタンが押された場合、マウスモードに切り替える
     */
    public static void checkMouseInput() {
        if (!enabled || !controllerInputActive) {
            return;
        }

        // ウィンドウがフォーカスされていない場合は無視
        if (!org.lwjgl.opengl.Display.isActive()) {
            return;
        }

        // マウスボタンが押されたらマウスモードに切り替え
        if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
            controllerInputActive = false;
            ControllerMod.LOG.info("Input mode: Mouse (click)");
        }
    }

    /**
     * 仮想カーソルのディスプレイX座標を取得する
     *
     * <p>
     * GUI座標系からディスプレイ座標系に変換して返す
     * Mouse.getX()の置換用
     *
     * @return ディスプレイX座標
     */
    public static int getDisplayX() {
        if (instance == null) {
            return 0;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaleFactor = sr.getScaleFactor();

        // GUI座標系からディスプレイ座標系に変換
        return (int) (instance.getCursorX() * scaleFactor);
    }

    /**
     * 仮想カーソルのディスプレイY座標を取得する
     *
     * <p>
     * GUI座標系からディスプレイ座標系に変換して返す
     * Y座標は反転する必要がある (ディスプレイ座標系は左下原点)
     * Mouse.getY()の置換用
     *
     * @return ディスプレイY座標
     */
    public static int getDisplayY() {
        if (instance == null) {
            return 0;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaleFactor = sr.getScaleFactor();
        int scaledHeight = sr.getScaledHeight();

        // GUI座標系からディスプレイ座標系に変換 (Y軸反転)
        float guiY = instance.getCursorY();
        return (int) ((scaledHeight - guiY) * scaleFactor);
    }

    /**
     * 仮想マウスボタンが押されているかどうかを返す
     *
     * <p>
     * Mouse.isButtonDown()の置換用
     *
     * @param button マウスボタン番号 (0=左, 1=右)
     * @return 押されている場合は {@code true}
     */
    public static boolean isMouseButtonDown(int button) {
        if (instance == null) {
            return false;
        }
        return instance.isMouseButtonDown(button);
    }

    /**
     * GUI座標系でのカーソルX座標を取得する
     *
     * @return GUI座標系のX座標
     */
    public static int getGuiX() {
        if (instance == null) {
            return 0;
        }
        return (int) instance.getCursorX();
    }

    /**
     * GUI座標系でのカーソルY座標を取得する
     *
     * @return GUI座標系のY座標
     */
    public static int getGuiY() {
        if (instance == null) {
            return 0;
        }
        return (int) instance.getCursorY();
    }

    /**
     * handleMouseInputをバーチャルカーソルで処理する
     *
     * <p>
     * MixinGuiScreenから呼び出される
     * 標準のhandleMouseInput処理をバイパスし、
     * バーチャルカーソルの座標を使用してマウスイベントを処理する
     *
     * @param screen 現在のGuiScreen
     */
    public static void handleMouseInput(GuiScreen screen) {
        // バーチャルカーソルの処理はVirtualCursor.update()で行われるため、
        // ここでは何もしない (標準のhandleMouseInputをキャンセルするだけ)
        //
        // 注: ボタン押下やドラッグはVirtualCursor.handleButtons()で
        // reflectionを使って直接mouseClicked等を呼び出している
    }

    // ========================================
    // ASM用ラッパーメソッド (Mouse.getX/Y/isButtonDownの置換先)
    // ========================================

    /**
     * マウスX座標を取得する (ASM置換用)
     *
     * <p>
     * コントローラーモード時はバーチャルカーソルの座標を返し、
     * それ以外は実マウスの座標を返す
     * ASMによりMouse.getX()の呼び出しがこのメソッドに置換される
     *
     * @return マウスX座標 (ディスプレイ座標系)
     */
    public static int getMouseX() {
        if (isControllerInputActive()) {
            return getDisplayX();
        }
        return Mouse.getX();
    }

    /**
     * マウスY座標を取得する (ASM置換用)
     *
     * <p>
     * コントローラーモード時はバーチャルカーソルの座標を返し、
     * それ以外は実マウスの座標を返す
     * ASMによりMouse.getY()の呼び出しがこのメソッドに置換される
     *
     * @return マウスY座標 (ディスプレイ座標系)
     */
    public static int getMouseY() {
        if (isControllerInputActive()) {
            return getDisplayY();
        }
        return Mouse.getY();
    }

    /**
     * マウスボタンが押されているかを取得する (ASM置換用)
     *
     * <p>
     * 実マウスまたはバーチャルカーソルのどちらかでボタンが押されていればtrueを返す
     * ASMによりMouse.isButtonDown()の呼び出しがこのメソッドに置換される
     *
     * @param button マウスボタン番号 (0=左, 1=右)
     * @return ボタンが押されている場合は {@code true}
     */
    public static boolean getMouseButtonDown(int button) {
        // 実マウスのボタン状態
        if (Mouse.isButtonDown(button)) {
            return true;
        }
        // コントローラーのボタン状態
        if (isActive() && isMouseButtonDown(button)) {
            return true;
        }
        return false;
    }
}
