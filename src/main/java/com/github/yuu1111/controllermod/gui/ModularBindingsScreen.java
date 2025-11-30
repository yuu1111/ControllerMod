package com.github.yuu1111.controllermod.gui;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.github.yuu1111.controllermod.config.BindingConfig;
import com.github.yuu1111.controllermod.input.ControllerBinding;
import com.github.yuu1111.controllermod.input.ControllerBindings;

/**
 * ModularUI2を使用したコントローラーバインド設定画面
 *
 * <p>
 * カテゴリ別にバインドを表示し、変更可能にする。
 * ボタンをクリックしてコントローラーのボタンを押すとリバインドされる。
 *
 * @author yuu1111
 */
public class ModularBindingsScreen {

    /** パネル名 */
    private static final String PANEL_NAME = "controller_bindings";

    /** パネル幅 */
    private static final int PANEL_WIDTH = 300;

    /** パネル高さ */
    private static final int PANEL_HEIGHT = 220;

    /** 現在リバインド中のバインド (null = リバインド中でない) */
    private static ControllerBinding rebindingTarget = null;

    /**
     * GUI画面を開く
     */
    public static void open() {
        rebindingTarget = null;
        ClientGUI.open(createScreen());
    }

    /**
     * ModularScreenを作成
     */
    private static ModularScreen createScreen() {
        return new ModularScreen(PANEL_NAME, context -> createMainPanel());
    }

    /**
     * メインパネルを作成
     */
    private static ModularPanel createMainPanel() {
        ModularPanel panel = ModularPanel.defaultPanel(PANEL_NAME, PANEL_WIDTH, PANEL_HEIGHT);

        // タイトル
        TextWidget titleWidget = new TextWidget(IKey.lang("controllermod.gui.bindings.title"))
            .alignment(Alignment.Center);
        titleWidget.size(PANEL_WIDTH - 16, 16);

        // バインドリスト
        ListWidget<IWidget, ?> bindingList = createBindingList();
        bindingList.size(PANEL_WIDTH - 16, PANEL_HEIGHT - 70);

        // ボタン行
        Row buttonRow = createButtonRow();
        buttonRow.size(PANEL_WIDTH - 16, 24);

        // パネルに追加
        Column mainColumn = new Column();
        mainColumn.widthRel(1f);
        mainColumn.heightRel(1f);
        mainColumn.padding(8);
        mainColumn.child(titleWidget);
        mainColumn.child(bindingList);
        mainColumn.child(buttonRow);

        panel.child(mainColumn);

        return panel;
    }

    /**
     * バインドリストを作成
     */
    @SuppressWarnings("unchecked")
    private static ListWidget<IWidget, ?> createBindingList() {
        ListWidget<IWidget, ?> list = new ListWidget<>();

        for (String category : ControllerBindings.getCategories()) {
            // カテゴリヘッダー
            TextWidget categoryText = new TextWidget(IKey.lang("controllermod.binding.category." + category))
                .color(0xFFFF00)
                .alignment(Alignment.CenterLeft);
            categoryText.size(PANEL_WIDTH - 32, 20);
            ((ListWidget<IWidget, ?>) list).child(categoryText);

            // バインド項目
            for (ControllerBinding binding : ControllerBindings.getByCategory(category)) {
                IWidget bindingRow = createBindingRow(binding);
                ((ListWidget<IWidget, ?>) list).child(bindingRow);
            }
        }

        return list;
    }

    /**
     * バインド行を作成
     */
    private static IWidget createBindingRow(ControllerBinding binding) {
        Row row = new Row();

        // バインド名
        TextWidget nameWidget = new TextWidget(IKey.lang(binding.getTranslationKey())).alignment(Alignment.CenterLeft);
        nameWidget.size(150, 18);

        // バインドボタン
        ButtonWidget<?> bindButton = new ButtonWidget<>();
        bindButton.size(80, 16);
        bindButton.onMousePressed(mouseButton -> {
            if (mouseButton == 0) {
                if (rebindingTarget == binding) {
                    // 既にリバインド中なら解除
                    rebindingTarget = null;
                } else {
                    // リバインドモード開始
                    rebindingTarget = binding;
                }
                return true;
            }
            return false;
        });

        // ボタンにテキストを追加
        TextWidget buttonText = new TextWidget(IKey.dynamic(() -> getButtonText(binding))).alignment(Alignment.Center)
            .color(() -> getButtonTextColor(binding));
        bindButton.child(buttonText);

        row.child(nameWidget);
        row.child(bindButton);
        row.size(PANEL_WIDTH - 32, 18);
        return row;
    }

    /**
     * ボタンテキストを取得
     */
    private static String getButtonText(ControllerBinding binding) {
        if (rebindingTarget == binding) {
            return "> ... <";
        }
        return ControllerBindings.getButtonName(binding.getButton());
    }

    /**
     * ボタンテキストの色を取得
     */
    private static int getButtonTextColor(ControllerBinding binding) {
        if (rebindingTarget == binding) {
            return 0x000000; // リバインド中は黒
        }
        if (binding.isModified()) {
            return 0xFFFF55; // 変更済みは黄色
        }
        return 0xFFFFFF; // デフォルトは白
    }

    /**
     * ボタン行を作成 (完了・リセット)
     */
    private static Row createButtonRow() {
        Row row = new Row();

        // 完了ボタン
        ButtonWidget<?> doneButton = new ButtonWidget<>();
        doneButton.size(100, 20);
        doneButton.onMousePressed(mouseButton -> {
            if (mouseButton == 0) {
                BindingConfig.save();
                ClientGUI.close();
                return true;
            }
            return false;
        });

        TextWidget doneText = new TextWidget(IKey.lang("gui.done")).alignment(Alignment.Center);
        doneButton.child(doneText);

        // リセットボタン
        ButtonWidget<?> resetButton = new ButtonWidget<>();
        resetButton.size(100, 20);
        resetButton.onMousePressed(mouseButton -> {
            if (mouseButton == 0) {
                ControllerBindings.resetAll();
                rebindingTarget = null;
                return true;
            }
            return false;
        });

        TextWidget resetText = new TextWidget(IKey.lang("controllermod.gui.bindings.reset"))
            .alignment(Alignment.Center);
        resetButton.child(resetText);

        row.child(doneButton);
        row.child(resetButton);

        return row;
    }

    /**
     * コントローラーボタンが押された時に呼ばれる
     *
     * @param button ボタンインデックス
     */
    public static void onControllerButton(int button) {
        if (rebindingTarget != null) {
            rebindingTarget.setButton(button);
            rebindingTarget = null;
        }
    }

    /**
     * リバインド中かどうか
     */
    public static boolean isRebinding() {
        return rebindingTarget != null;
    }
}
