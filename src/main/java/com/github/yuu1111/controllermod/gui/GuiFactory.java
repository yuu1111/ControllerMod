package com.github.yuu1111.controllermod.gui;

import net.minecraft.client.gui.GuiScreen;

import com.github.yuu1111.controllermod.config.ControllerConfig;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiFactory;

/**
 * Mod設定画面のファクトリークラス
 *
 * <p>
 * GTNHLibのSimpleGuiFactoryを使用して、Forgeの「Mods」→「Config」ボタンから
 * 自動生成された設定画面を開けるようにする。
 *
 * @author yuu1111
 */
public class GuiFactory implements SimpleGuiFactory {

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ControllerConfigGui.class;
    }

    /**
     * GTNHLibベースの設定GUI
     */
    public static class ControllerConfigGui extends SimpleGuiConfig {

        public ControllerConfigGui(GuiScreen parentScreen) throws ConfigException {
            super(parentScreen, "controllermod", "Controller Settings", ControllerConfig.class);
        }
    }
}
