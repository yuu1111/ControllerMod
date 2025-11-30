package com.github.yuu1111.controllermod.asm;

import static com.github.yuu1111.controllermod.constants.Constants.MC_VERSION;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

/**
 * ControllerMod用Coremodプラグイン
 *
 * <p>
 * Mouse.getX(), getY(), isButtonDown() の呼び出しを
 * VirtualCursorManagerのメソッドに置き換えるASMトランスフォーマーを登録する
 */
@IFMLLoadingPlugin.MCVersion(MC_VERSION)
@IFMLLoadingPlugin.TransformerExclusions({ "com.github.yuu1111.controllermod.asm" })
@IFMLLoadingPlugin.SortingIndex(1001)
public class ControllerModPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "com.github.yuu1111.controllermod.asm.MouseCallTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // 特に必要なし
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
