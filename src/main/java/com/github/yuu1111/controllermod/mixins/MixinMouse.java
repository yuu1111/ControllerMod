package com.github.yuu1111.controllermod.mixins;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.yuu1111.controllermod.ControllerMod;
import com.github.yuu1111.controllermod.gui.VirtualCursorManager;

/**
 * LWJGL MouseクラスへのMixin注入
 *
 * <p>
 * Mouse.getX(), getY(), isButtonDown() を拡張し、
 * コントローラー入力中はバーチャルカーソルの座標/状態を使用する。
 * 実マウスとバーチャルカーソルが共存できるようにする。
 *
 * <p>
 * 注意: LWJGLクラスは難読化されないため、remap = false を指定する。
 *
 * @author yuu1111
 */
@Mixin(value = Mouse.class, remap = false)
public class MixinMouse {

    static {
        System.out.println("[ControllerMod] MixinMouse class loaded!");
    }

    /**
     * Mouse.getX() を拡張
     *
     * <p>
     * コントローラー入力モード中はバーチャルカーソルのX座標を返す。
     *
     * @param cir コールバック情報
     */
    /** デバッグログのスロットリング用カウンター */
    private static int debugCounter = 0;

    @Inject(method = "getX", at = @At("HEAD"), cancellable = true)
    private static void controllermod$onGetX(CallbackInfoReturnable<Integer> cir) {
        if (VirtualCursorManager.isControllerInputActive()) {
            int displayX = VirtualCursorManager.getDisplayX();
            // デバッグログ（60フレームごと）
            if (debugCounter % 60 == 0) {
                ControllerMod.LOG.info("MixinMouse.getX() called - returning virtual X: {}", displayX);
            }
            cir.setReturnValue(displayX);
        }
    }

    /**
     * Mouse.getY() を拡張
     *
     * <p>
     * コントローラー入力モード中はバーチャルカーソルのY座標を返す。
     *
     * @param cir コールバック情報
     */
    @Inject(method = "getY", at = @At("HEAD"), cancellable = true)
    private static void controllermod$onGetY(CallbackInfoReturnable<Integer> cir) {
        if (VirtualCursorManager.isControllerInputActive()) {
            int displayY = VirtualCursorManager.getDisplayY();
            // デバッグログ（60フレームごと）
            if (debugCounter % 60 == 0) {
                ControllerMod.LOG.info("MixinMouse.getY() called - returning virtual Y: {}", displayY);
            }
            debugCounter++;
            cir.setReturnValue(displayY);
        }
    }

    /**
     * Mouse.isButtonDown() を拡張
     *
     * <p>
     * 実マウスのボタン状態に加えて、バーチャルカーソルのボタン状態も考慮する。
     * どちらかが押されていればtrueを返す（OR条件）。
     *
     * @param button マウスボタン番号
     * @param cir    コールバック情報
     */
    /** isButtonDownデバッグログ用カウンター */
    private static int buttonDebugCounter = 0;

    @Inject(method = "isButtonDown", at = @At("RETURN"), cancellable = true)
    private static void controllermod$onIsButtonDown(int button, CallbackInfoReturnable<Boolean> cir) {
        // 実マウスが押されていなくても、コントローラーボタンが押されていればtrue
        if (!cir.getReturnValue() && VirtualCursorManager.isActive()) {
            boolean virtualButton = VirtualCursorManager.isMouseButtonDown(button);
            if (virtualButton) {
                // デバッグログ（ボタン押下時のみ、10回ごと）
                if (buttonDebugCounter % 10 == 0) {
                    ControllerMod.LOG.info("MixinMouse.isButtonDown({}) - virtual button pressed!", button);
                }
                buttonDebugCounter++;
                cir.setReturnValue(true);
            }
        }
    }
}
