package com.github.yuu1111.controllermod.asm;

import org.lwjgl.input.Mouse;

import com.github.yuu1111.controllermod.gui.VirtualCursorManager;

/**
 * Mouse API のフッククラス (ASM置換先)
 *
 * <p>
 * ASMトランスフォーマーにより Mouse.getX(), Mouse.getY(), Mouse.isButtonDown() の
 * 呼び出しがこのクラスのメソッドに置換される
 *
 * <p>
 * コントローラーモード時はバーチャルカーソルの値を、
 * そうでなければ実マウスの値を返す
 *
 * @author yuu1111
 */
public final class MouseHook {

    private MouseHook() {
        // ユーティリティクラス
    }

    /**
     * マウスX座標を取得する
     *
     * <p>
     * ASMにより Mouse.getX() の呼び出しがこのメソッドに置換される
     *
     * @return マウスX座標 (ディスプレイ座標系)
     */
    public static int getX() {
        if (VirtualCursorManager.isControllerInputActive()) {
            return VirtualCursorManager.getDisplayX();
        }
        return Mouse.getX();
    }

    /**
     * マウスY座標を取得する
     *
     * <p>
     * ASMにより Mouse.getY() の呼び出しがこのメソッドに置換される
     *
     * @return マウスY座標 (ディスプレイ座標系)
     */
    public static int getY() {
        if (VirtualCursorManager.isControllerInputActive()) {
            return VirtualCursorManager.getDisplayY();
        }
        return Mouse.getY();
    }

    /**
     * マウスボタンが押されているかを取得する
     *
     * <p>
     * ASMにより Mouse.isButtonDown() の呼び出しがこのメソッドに置換される
     * 実マウスまたはバーチャルカーソルのどちらかでボタンが押されていればtrueを返す
     *
     * @param button マウスボタン番号 (0=左, 1=右)
     * @return ボタンが押されている場合は {@code true}
     */
    public static boolean isButtonDown(int button) {
        // 実マウスのボタン状態
        if (Mouse.isButtonDown(button)) {
            return true;
        }
        // コントローラーのボタン状態
        if (VirtualCursorManager.isActive()) {
            return VirtualCursorManager.isMouseButtonDown(button);
        }
        return false;
    }
}
