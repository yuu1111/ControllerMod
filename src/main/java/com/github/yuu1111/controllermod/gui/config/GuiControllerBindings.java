package com.github.yuu1111.controllermod.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;

import com.github.yuu1111.controllermod.config.BindingConfig;
import com.github.yuu1111.controllermod.input.ControllerBinding;
import com.github.yuu1111.controllermod.input.ControllerBindings;

/**
 * コントローラーバインド設定GUI
 *
 * <p>
 * カテゴリ別にバインドを表示し、変更可能にする。
 * ボタンをクリックしてコントローラーのボタンを押すとリバインドされる。
 */
public class GuiControllerBindings extends GuiScreen {

    /** 親画面 (戻る時に表示) */
    private final GuiScreen parent;

    /** バインドボタンのリスト */
    private final List<BindingButton> bindingButtons = new ArrayList<>();

    /** 現在リバインド中のバインド (null = リバインド中でない) */
    private ControllerBinding rebindingTarget = null;

    /** スクロールオフセット */
    private int scrollOffset = 0;

    /** 表示可能な行数 */
    private int visibleRows = 0;

    /** 総行数 (カテゴリヘッダー + バインド) */
    private int totalRows = 0;

    /** 行の高さ */
    private static final int ROW_HEIGHT = 24;

    /** ボタンID: 完了 */
    private static final int BUTTON_DONE = 0;

    /** ボタンID: リセット */
    private static final int BUTTON_RESET = 1;

    /**
     * コンストラクタ
     *
     * @param parent 親画面
     */
    public GuiControllerBindings(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        bindingButtons.clear();

        // 表示可能行数を計算
        int headerHeight = 40;
        int footerHeight = 40;
        visibleRows = (height - headerHeight - footerHeight) / ROW_HEIGHT;

        // 総行数を計算 (カテゴリ + バインド)
        totalRows = 0;
        for (String category : ControllerBindings.getCategories()) {
            totalRows++; // カテゴリヘッダー
            totalRows += ControllerBindings.getByCategory(category)
                .size();
        }

        // 完了ボタン
        buttonList.add(new GuiButton(BUTTON_DONE, width / 2 - 155, height - 30, 150, 20, I18n.format("gui.done")));

        // リセットボタン
        buttonList.add(
            new GuiButton(
                BUTTON_RESET,
                width / 2 + 5,
                height - 30,
                150,
                20,
                I18n.format("controllermod.gui.bindings.reset")));

        // バインドボタンを生成
        createBindingButtons();
    }

    /**
     * バインドボタンを生成
     */
    private void createBindingButtons() {
        bindingButtons.clear();

        int buttonWidth = 80;
        int buttonX = width / 2 + 60;
        int row = 0;

        for (String category : ControllerBindings.getCategories()) {
            row++; // カテゴリヘッダー行
            for (ControllerBinding binding : ControllerBindings.getByCategory(category)) {
                BindingButton btn = new BindingButton(binding, buttonX, row, buttonWidth);
                bindingButtons.add(btn);
                row++;
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BUTTON_DONE) {
            BindingConfig.save();
            mc.displayGuiScreen(parent);
        } else if (button.id == BUTTON_RESET) {
            ControllerBindings.resetAll();
            rebindingTarget = null;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // タイトル
        drawCenteredString(fontRendererObj, I18n.format("controllermod.gui.bindings.title"), width / 2, 15, 0xFFFFFF);

        // バインドリストを描画
        int startY = 40;
        int row = 0;
        int visibleStart = scrollOffset;
        int visibleEnd = scrollOffset + visibleRows;

        for (String category : ControllerBindings.getCategories()) {
            // カテゴリヘッダー
            if (row >= visibleStart && row < visibleEnd) {
                int y = startY + (row - scrollOffset) * ROW_HEIGHT;
                String categoryName = I18n.format("controllermod.binding.category." + category);
                drawString(fontRendererObj, "§e§l" + categoryName, width / 2 - 150, y + 6, 0xFFFFFF);
            }
            row++;

            // バインド項目
            for (ControllerBinding binding : ControllerBindings.getByCategory(category)) {
                if (row >= visibleStart && row < visibleEnd) {
                    int y = startY + (row - scrollOffset) * ROW_HEIGHT;
                    drawBindingRow(binding, y, mouseX, mouseY);
                }
                row++;
            }
        }

        // スクロールバー
        if (totalRows > visibleRows) {
            int scrollBarHeight = height - 80;
            int scrollBarX = width - 10;
            int scrollBarY = 40;
            float scrollRatio = (float) scrollOffset / (totalRows - visibleRows);
            int thumbHeight = Math.max(20, scrollBarHeight * visibleRows / totalRows);
            int thumbY = scrollBarY + (int) ((scrollBarHeight - thumbHeight) * scrollRatio);

            drawRect(scrollBarX, scrollBarY, scrollBarX + 6, scrollBarY + scrollBarHeight, 0x40FFFFFF);
            drawRect(scrollBarX, thumbY, scrollBarX + 6, thumbY + thumbHeight, 0xFFAAAAAA);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * バインド行を描画
     */
    private void drawBindingRow(ControllerBinding binding, int y, int mouseX, int mouseY) {
        // バインド名
        String name = I18n.format(binding.getTranslationKey());
        drawString(fontRendererObj, name, width / 2 - 140, y + 6, 0xFFFFFF);

        // ボタン表示
        int buttonX = width / 2 + 60;
        int buttonWidth = 80;
        boolean isRebinding = rebindingTarget == binding;
        boolean isHovered = mouseX >= buttonX && mouseX < buttonX + buttonWidth && mouseY >= y && mouseY < y + 20;

        // ボタン背景
        int bgColor = isRebinding ? 0xFFFFFF00 : (isHovered ? 0xFF6688AA : 0xFF404040);
        drawRect(buttonX, y, buttonX + buttonWidth, y + 20, bgColor);

        // ボタンテキスト
        String buttonText;
        int textColor;
        if (isRebinding) {
            buttonText = "> ... <";
            textColor = 0xFF000000;
        } else {
            buttonText = ControllerBindings.getButtonName(binding.getButton());
            textColor = binding.isModified() ? 0xFFFFFF55 : 0xFFFFFFFF;
        }
        int textWidth = fontRendererObj.getStringWidth(buttonText);
        fontRendererObj.drawString(buttonText, buttonX + (buttonWidth - textWidth) / 2, y + 6, textColor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) {
            return;
        }

        // リバインド中なら解除
        if (rebindingTarget != null) {
            rebindingTarget = null;
            return;
        }

        // バインドボタンのクリック判定
        int startY = 40;
        int row = 0;
        int visibleStart = scrollOffset;
        int visibleEnd = scrollOffset + visibleRows;
        int buttonX = width / 2 + 60;
        int buttonWidth = 80;

        for (String category : ControllerBindings.getCategories()) {
            row++; // カテゴリヘッダー
            for (ControllerBinding binding : ControllerBindings.getByCategory(category)) {
                if (row >= visibleStart && row < visibleEnd) {
                    int y = startY + (row - scrollOffset) * ROW_HEIGHT;
                    if (mouseX >= buttonX && mouseX < buttonX + buttonWidth && mouseY >= y && mouseY < y + 20) {
                        rebindingTarget = binding;
                        return;
                    }
                }
                row++;
            }
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        // マウスホイールでスクロール
        int wheel = org.lwjgl.input.Mouse.getDWheel();
        if (wheel != 0) {
            if (wheel > 0) {
                scrollOffset = Math.max(0, scrollOffset - 2);
            } else {
                scrollOffset = Math.min(Math.max(0, totalRows - visibleRows), scrollOffset + 2);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // Escでリバインドキャンセルまたは画面を閉じる
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (rebindingTarget != null) {
                rebindingTarget = null;
            } else {
                BindingConfig.save();
                mc.displayGuiScreen(parent);
            }
        }
    }

    /**
     * コントローラーボタンが押された時に呼ばれる
     *
     * @param button ボタンインデックス
     */
    public void onControllerButton(int button) {
        if (rebindingTarget != null) {
            rebindingTarget.setButton(button);
            rebindingTarget = null;
        }
    }

    /**
     * リバインド中かどうか
     */
    public boolean isRebinding() {
        return rebindingTarget != null;
    }

    /**
     * バインドボタンの情報
     */
    private static class BindingButton {

        final ControllerBinding binding;
        final int x;
        final int row;
        final int width;

        BindingButton(ControllerBinding binding, int x, int row, int width) {
            this.binding = binding;
            this.x = x;
            this.row = row;
            this.width = width;
        }
    }
}
