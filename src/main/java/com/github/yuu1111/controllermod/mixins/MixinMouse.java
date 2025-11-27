package com.github.yuu1111.controllermod.mixins;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.yuu1111.controllermod.gui.VirtualCursorManager;

/**
 * LWJGL MouseクラスへのMixin注入
 *
 * <p>
 * Mouse.getX(), getY(), isButtonDown() を仮想座標/状態で置換する。
 * これにより、GuiSlot等の直接Mouse座標を参照するGUIでも
 * バーチャルカーソルが動作するようになる。
 *
 * <p>
 * 注意: LWJGLクラスは難読化されないため、remap = false を指定する。
 *
 * @author yuu1111
 */
@Mixin(value = Mouse.class, remap = false)
public class MixinMouse {

    /**
     * Mouse.getX() を仮想X座標で置換
     *
     * @param cir コールバック情報
     */
    @Inject(method = "getX", at = @At("HEAD"), cancellable = true)
    private static void controllermod$onGetX(CallbackInfoReturnable<Integer> cir) {
        if (VirtualCursorManager.isActive()) {
            cir.setReturnValue(VirtualCursorManager.getDisplayX());
        }
    }

    /**
     * Mouse.getY() を仮想Y座標で置換
     *
     * @param cir コールバック情報
     */
    @Inject(method = "getY", at = @At("HEAD"), cancellable = true)
    private static void controllermod$onGetY(CallbackInfoReturnable<Integer> cir) {
        if (VirtualCursorManager.isActive()) {
            cir.setReturnValue(VirtualCursorManager.getDisplayY());
        }
    }

    /**
     * Mouse.isButtonDown() を仮想ボタン状態で置換
     *
     * @param button マウスボタン番号
     * @param cir    コールバック情報
     */
    @Inject(method = "isButtonDown", at = @At("HEAD"), cancellable = true)
    private static void controllermod$onIsButtonDown(int button, CallbackInfoReturnable<Boolean> cir) {
        if (VirtualCursorManager.isActive()) {
            cir.setReturnValue(VirtualCursorManager.isMouseButtonDown(button));
        }
    }

    /**
     * Mouse.getDX() を0で置換 (バーチャルカーソル中は移動量なし)
     *
     * @param cir コールバック情報
     */
    @Inject(method = "getDX", at = @At("HEAD"), cancellable = true)
    private static void controllermod$onGetDX(CallbackInfoReturnable<Integer> cir) {
        if (VirtualCursorManager.isActive()) {
            cir.setReturnValue(0);
        }
    }

    /**
     * Mouse.getDY() を0で置換 (バーチャルカーソル中は移動量なし)
     *
     * @param cir コールバック情報
     */
    @Inject(method = "getDY", at = @At("HEAD"), cancellable = true)
    private static void controllermod$onGetDY(CallbackInfoReturnable<Integer> cir) {
        if (VirtualCursorManager.isActive()) {
            cir.setReturnValue(0);
        }
    }
}
