package com.github.yuu1111.controllermod.gui.config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import com.github.yuu1111.controllermod.config.ControllerConfig;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;

/**
 * コントローラーMod設定画面
 *
 * <p>
 * GTNHLibの設定GUIとバインド設定へのボタンを提供する。
 */
public class GuiControllerConfig extends GuiScreen {

    /** 親画面 */
    private final GuiScreen parent;

    /** GTNHLib設定GUI */
    private SimpleGuiConfig configGui;

    /** ボタンID: 一般設定 */
    private static final int BUTTON_GENERAL = 0;

    /** ボタンID: バインド設定 */
    private static final int BUTTON_BINDINGS = 1;

    /** ボタンID: 完了 */
    private static final int BUTTON_DONE = 2;

    /**
     * コンストラクタ
     *
     * @param parent 親画面
     */
    public GuiControllerConfig(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        int centerX = width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;

        // 一般設定ボタン
        buttonList.add(
            new GuiButton(
                BUTTON_GENERAL,
                centerX - buttonWidth / 2,
                height / 2 - 35,
                buttonWidth,
                buttonHeight,
                I18n.format("controllermod.gui.config.general")));

        // バインド設定ボタン
        buttonList.add(
            new GuiButton(
                BUTTON_BINDINGS,
                centerX - buttonWidth / 2,
                height / 2 - 10,
                buttonWidth,
                buttonHeight,
                I18n.format("controllermod.gui.config.bindings")));

        // 完了ボタン
        buttonList.add(
            new GuiButton(
                BUTTON_DONE,
                centerX - buttonWidth / 2,
                height / 2 + 30,
                buttonWidth,
                buttonHeight,
                I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case BUTTON_GENERAL:
                try {
                    mc.displayGuiScreen(new GeneralConfigGui(this));
                } catch (ConfigException e) {
                    // エラー時は何もしない
                }
                break;
            case BUTTON_BINDINGS:
                mc.displayGuiScreen(new GuiControllerBindings(this));
                break;
            case BUTTON_DONE:
                mc.displayGuiScreen(parent);
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("controllermod.gui.config.title"), width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * 一般設定GUI (GTNHLib)
     */
    public static class GeneralConfigGui extends SimpleGuiConfig {

        public GeneralConfigGui(GuiScreen parentScreen) throws ConfigException {
            super(parentScreen, "controllermod", "General Settings", ControllerConfig.class);
        }
    }
}
