package com.github.yuu1111.controllermod.mixins;

import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.yuu1111.controllermod.gui.VirtualCursorManager;

/**
 * GuiScreenへのMixin注入
 *
 * <p>
 * handleMouseInputメソッドの先頭に注入し、
 * バーチャルカーソルがアクティブな場合は標準のマウス処理をスキップする。
 *
 * @author yuu1111
 */
@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

    /**
     * handleMouseInputの先頭に注入
     *
     * <p>
     * バーチャルカーソルがアクティブな場合、
     * 標準のマウス入力処理をキャンセルし、
     * バーチャルカーソルの座標を使用した処理を行う。
     *
     * @param ci コールバック情報
     */
    @Inject(method = "handleMouseInput", at = @At("HEAD"), cancellable = true)
    private void controllermod$onHandleMouseInput(CallbackInfo ci) {
        if (VirtualCursorManager.isActive()) {
            // バーチャルカーソルで処理し、標準処理をキャンセル
            VirtualCursorManager.handleMouseInput((GuiScreen) (Object) this);
            ci.cancel();
        }
    }
}
