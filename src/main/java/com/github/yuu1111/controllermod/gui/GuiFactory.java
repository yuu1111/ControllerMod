package com.github.yuu1111.controllermod.gui;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.SimpleGuiFactory;

/**
 * Mod設定画面のファクトリークラス
 *
 * <p>
 * GTNHLibのSimpleGuiFactoryを使用して、Forgeの「Mods」→「Config」ボタンから
 * 設定画面を開けるようにする。
 */
public class GuiFactory implements SimpleGuiFactory {

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return GuiControllerConfig.class;
    }
}
