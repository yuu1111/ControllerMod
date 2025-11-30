package com.github.yuu1111.controllermod.mixins;

import net.minecraft.client.gui.GuiSlot;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.yuu1111.controllermod.ControllerMod;
import com.github.yuu1111.controllermod.gui.VirtualCursorManager;

/**
 * GuiSlotクラスへのMixin注入
 *
 * <p>
 * GuiSlot.drawScreen() 内の Mouse.getX(), getY(), isButtonDown() 呼び出しを
 * リダイレクトし、コントローラー入力中はバーチャルカーソルの座標/状態を使用する。
 *
 * <p>
 * 注意: LWJGLのMouseクラスは直接Mixinできないため、呼び出し元であるGuiSlotを
 * ターゲットにしてリダイレクトする。
 *
 * @author yuu1111
 */
@Mixin(GuiSlot.class)
public class MixinGuiSlot {

    /** デバッグログのスロットリング用カウンター */
    private static int debugCounter = 0;

    /**
     * Mouse.getX() をリダイレクト
     *
     * <p>
     * コントローラー入力モード中はバーチャルカーソルのX座標を返す。
     *
     * @return X座標（ディスプレイ座標系）
     */
    @Redirect(
        method = { "drawScreen", "func_148128_a" },
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getX()I", remap = false))
    private int controllermod$redirectGetX() {
        if (VirtualCursorManager.isControllerInputActive()) {
            int displayX = VirtualCursorManager.getDisplayX();
            // デバッグログ（60フレームごと）
            if (debugCounter % 60 == 0) {
                ControllerMod.LOG.info("GuiSlot.getX() redirected - returning virtual X: {}", displayX);
            }
            return displayX;
        }
        return Mouse.getX();
    }

    /**
     * Mouse.getY() をリダイレクト
     *
     * <p>
     * コントローラー入力モード中はバーチャルカーソルのY座標を返す。
     *
     * @return Y座標（ディスプレイ座標系）
     */
    @Redirect(
        method = { "drawScreen", "func_148128_a" },
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getY()I", remap = false))
    private int controllermod$redirectGetY() {
        if (VirtualCursorManager.isControllerInputActive()) {
            int displayY = VirtualCursorManager.getDisplayY();
            // デバッグログ（60フレームごと）
            if (debugCounter % 60 == 0) {
                ControllerMod.LOG.info("GuiSlot.getY() redirected - returning virtual Y: {}", displayY);
            }
            debugCounter++;
            return displayY;
        }
        return Mouse.getY();
    }

    /**
     * Mouse.isButtonDown() をリダイレクト
     *
     * <p>
     * 実マウスのボタン状態に加えて、バーチャルカーソルのボタン状態も考慮する。
     * どちらかが押されていればtrueを返す（OR条件）。
     *
     * @param button マウスボタン番号
     * @return ボタンが押されている場合は {@code true}
     */
    @Redirect(
        method = { "drawScreen", "func_148128_a" },
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z", remap = false))
    private boolean controllermod$redirectIsButtonDown(int button) {
        boolean realButton = Mouse.isButtonDown(button);

        // 実マウスが押されていなくても、コントローラーボタンが押されていればtrue
        if (!realButton && VirtualCursorManager.isActive()) {
            boolean virtualButton = VirtualCursorManager.isMouseButtonDown(button);
            if (virtualButton) {
                // デバッグログ（ボタン押下時）
                ControllerMod.LOG.info("GuiSlot.isButtonDown({}) redirected - virtual button pressed!", button);
                return true;
            }
        }

        return realButton;
    }
}
