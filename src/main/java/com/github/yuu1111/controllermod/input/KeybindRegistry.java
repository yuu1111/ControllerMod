package com.github.yuu1111.controllermod.input;

import static com.github.yuu1111.controllermod.constants.SDL2.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * コントローラーバインドのレジストリ
 *
 * <p>
 * 全てのバインド可能なアクションを管理する。
 * カテゴリ別にバインドを整理し、設定の保存/読み込みをサポートする。
 * 
 * @see SDL2
 */
public final class KeybindRegistry {

    // カテゴリ定数
    public static final String CATEGORY_MOVEMENT = "movement";
    public static final String CATEGORY_COMBAT = "combat";
    public static final String CATEGORY_GAMEPLAY = "gameplay";
    public static final String CATEGORY_GUI = "gui";
    public static final String CATEGORY_MISC = "misc";

    // バインド定義 - 移動系
    public static final Keybind JUMP = new Keybind(
        "jump",
        "controllermod.binding.jump",
        CATEGORY_MOVEMENT,
        BUTTON_A,
        true);

    public static final Keybind SNEAK = new Keybind(
        "sneak",
        "controllermod.binding.sneak",
        CATEGORY_MOVEMENT,
        BUTTON_B,
        true);

    public static final Keybind SPRINT = new Keybind(
        "sprint",
        "controllermod.binding.sprint",
        CATEGORY_MOVEMENT,
        BUTTON_L3,
        true);

    // バインド定義 - 戦闘系
    public static final Keybind ATTACK = new Keybind(
        "attack",
        "controllermod.binding.attack",
        CATEGORY_COMBAT,
        TRIGGER_RIGHT,
        true,
        true);

    public static final Keybind USE_ITEM = new Keybind(
        "use_item",
        "controllermod.binding.use_item",
        CATEGORY_COMBAT,
        TRIGGER_LEFT,
        true,
        true);

    // バインド定義 - ゲームプレイ系
    public static final Keybind INVENTORY = new Keybind(
        "inventory",
        "controllermod.binding.inventory",
        CATEGORY_GAMEPLAY,
        BUTTON_Y,
        false);

    public static final Keybind DROP_ITEM = new Keybind(
        "drop_item",
        "controllermod.binding.drop_item",
        CATEGORY_GAMEPLAY,
        BUTTON_DPAD_DOWN,
        true);

    public static final Keybind HOTBAR_NEXT = new Keybind(
        "hotbar_next",
        "controllermod.binding.hotbar_next",
        CATEGORY_GAMEPLAY,
        BUTTON_RB,
        false);

    public static final Keybind HOTBAR_PREV = new Keybind(
        "hotbar_prev",
        "controllermod.binding.hotbar_prev",
        CATEGORY_GAMEPLAY,
        BUTTON_LB,
        false);

    public static final Keybind TOGGLE_PERSPECTIVE = new Keybind(
        "toggle_perspective",
        "controllermod.binding.toggle_perspective",
        CATEGORY_GAMEPLAY,
        BUTTON_DPAD_UP,
        false);

    public static final Keybind OPEN_CHAT = new Keybind(
        "open_chat",
        "controllermod.binding.open_chat",
        CATEGORY_GAMEPLAY,
        BUTTON_DPAD_RIGHT,
        false);

    // バインド定義 - GUI系
    public static final Keybind GUI_SELECT = new Keybind(
        "gui_select",
        "controllermod.binding.gui_select",
        CATEGORY_GUI,
        BUTTON_A,
        false);

    public static final Keybind GUI_BACK = new Keybind(
        "gui_back",
        "controllermod.binding.gui_back",
        CATEGORY_GUI,
        BUTTON_B,
        false);

    // バインド定義 - その他
    public static final Keybind PAUSE = new Keybind(
        "pause",
        "controllermod.binding.pause",
        CATEGORY_MISC,
        BUTTON_START,
        false);

    public static final Keybind PLAYER_LIST = new Keybind(
        "player_list",
        "controllermod.binding.player_list",
        CATEGORY_MISC,
        BUTTON_BACK,
        true);

    /** 全バインドのマップ (ID -> Binding) */
    private static final Map<String, Keybind> BINDINGS = new LinkedHashMap<>();

    /** カテゴリ順序 */
    private static final List<String> CATEGORY_ORDER = new ArrayList<>();

    static {
        // カテゴリ順序を定義
        CATEGORY_ORDER.add(CATEGORY_MOVEMENT);
        CATEGORY_ORDER.add(CATEGORY_COMBAT);
        CATEGORY_ORDER.add(CATEGORY_GAMEPLAY);
        CATEGORY_ORDER.add(CATEGORY_GUI);
        CATEGORY_ORDER.add(CATEGORY_MISC);

        // バインドを登録
        register(JUMP);
        register(SNEAK);
        register(SPRINT);
        register(ATTACK);
        register(USE_ITEM);
        register(INVENTORY);
        register(DROP_ITEM);
        register(HOTBAR_NEXT);
        register(HOTBAR_PREV);
        register(TOGGLE_PERSPECTIVE);
        register(OPEN_CHAT);
        register(GUI_SELECT);
        register(GUI_BACK);
        register(PAUSE);
        register(PLAYER_LIST);
    }

    private KeybindRegistry() {
        // ユーティリティクラス
    }

    /**
     * バインドを登録
     */
    private static void register(Keybind binding) {
        BINDINGS.put(binding.getId(), binding);
    }

    /**
     * IDでバインドを取得
     */
    public static Keybind get(String id) {
        return BINDINGS.get(id);
    }

    /**
     * 全バインドを取得
     */
    public static List<Keybind> getAll() {
        return new ArrayList<>(BINDINGS.values());
    }

    /**
     * カテゴリ別にバインドを取得
     */
    public static List<Keybind> getByCategory(String category) {
        List<Keybind> result = new ArrayList<>();
        for (Keybind binding : BINDINGS.values()) {
            if (binding.getCategory()
                .equals(category)) {
                result.add(binding);
            }
        }
        return result;
    }

    /**
     * カテゴリ一覧を取得
     */
    public static List<String> getCategories() {
        return new ArrayList<>(CATEGORY_ORDER);
    }

    /**
     * 全バインドをデフォルトにリセット
     */
    public static void resetAll() {
        for (Keybind binding : BINDINGS.values()) {
            binding.reset();
        }
    }

    /**
     * 指定ボタンが特定のバインドに割り当てられているか確認
     *
     * @param button    ボタンインデックス
     * @param excludeId 除外するバインドID (null可)
     * @return 既に使用されている場合はそのバインド、なければnull
     */
    public static Keybind findConflict(int button, String excludeId) {
        for (Keybind binding : BINDINGS.values()) {
            if (binding.getId()
                .equals(excludeId)) {
                continue;
            }
            if (binding.getButton() == button) {
                return binding;
            }
        }
        return null;
    }

    /**
     * ボタンインデックスからボタン名を取得
     */
    public static String getButtonName(int button) {
        switch (button) {
            case BUTTON_A:
                return "A";
            case BUTTON_B:
                return "B";
            case BUTTON_X:
                return "X";
            case BUTTON_Y:
                return "Y";
            case BUTTON_BACK:
                return "Back";
            case BUTTON_GUIDE:
                return "Guide";
            case BUTTON_START:
                return "Start";
            case BUTTON_L3:
                return "L3";
            case BUTTON_R3:
                return "R3";
            case BUTTON_LB:
                return "LB";
            case BUTTON_RB:
                return "RB";
            case BUTTON_DPAD_UP:
                return "D-Up";
            case BUTTON_DPAD_DOWN:
                return "D-Down";
            case BUTTON_DPAD_LEFT:
                return "D-Left";
            case BUTTON_DPAD_RIGHT:
                return "D-Right";
            case TRIGGER_LEFT:
                return "LT";
            case TRIGGER_RIGHT:
                return "RT";
            case -1:
                return "---";
            default:
                return "?";
        }
    }
}
